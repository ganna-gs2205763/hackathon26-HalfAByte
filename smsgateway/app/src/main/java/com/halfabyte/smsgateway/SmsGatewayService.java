package com.halfabyte.smsgateway;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.halfabyte.smsgateway.models.LogEntry;
import com.halfabyte.smsgateway.models.SendSmsCommand;
import com.halfabyte.smsgateway.models.SmsSentConfirmation;

import java.util.ArrayList;
import java.util.List;

/**
 * Foreground service that keeps the SMS gateway running in the background.
 * Coordinates between SMS receiver, sender, and WebSocket manager.
 */
public class SmsGatewayService extends Service implements WebSocketManager.WebSocketCallback {
    private static final String TAG = "SmsGatewayService";
    private static final String CHANNEL_ID = "sms_gateway_channel";
    private static final int NOTIFICATION_ID = 1;

    public static final String ACTION_SMS_RECEIVED = "com.halfabyte.smsgateway.SMS_RECEIVED";
    public static final String ACTION_CONNECT = "com.halfabyte.smsgateway.CONNECT";
    public static final String ACTION_DISCONNECT = "com.halfabyte.smsgateway.DISCONNECT";
    public static final String EXTRA_SENDER = "sender";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_URL = "url";
    public static final String EXTRA_MOCK_MODE = "mock_mode";

    private final IBinder binder = new LocalBinder();
    private WebSocketManager webSocketManager;
    private SmsSender smsSender;
    private MockWebSocketServer mockServer;
    private boolean isMockMode = false;
    private ServiceCallback callback;
    
    // Buffer for log entries when Activity isn't connected
    private final List<LogEntry> logBuffer = new ArrayList<>();
    private static final int MAX_LOG_BUFFER = 100;

    public interface ServiceCallback {
        void onLogEntry(LogEntry entry);

        void onConnectionStateChanged(WebSocketManager.ConnectionState state);
    }

    public class LocalBinder extends Binder {
        public SmsGatewayService getService() {
            return SmsGatewayService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");

        createNotificationChannel();
        smsSender = new SmsSender(this);
        webSocketManager = new WebSocketManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started with action: " + (intent != null ? intent.getAction() : "null"));

        // Start as foreground service
        startForeground(NOTIFICATION_ID, createNotification());

        if (intent != null) {
            String action = intent.getAction();

            if (ACTION_SMS_RECEIVED.equals(action)) {
                String sender = intent.getStringExtra(EXTRA_SENDER);
                String message = intent.getStringExtra(EXTRA_MESSAGE);
                handleIncomingSms(sender, message);
            } else if (ACTION_CONNECT.equals(action)) {
                String url = intent.getStringExtra(EXTRA_URL);
                boolean mockMode = intent.getBooleanExtra(EXTRA_MOCK_MODE, false);
                connect(url, mockMode);
            } else if (ACTION_DISCONNECT.equals(action)) {
                disconnect();
            }
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        disconnect();
    }

    public void setCallback(ServiceCallback callback) {
        this.callback = callback;
        
        // Replay buffered logs when Activity connects
        if (callback != null && !logBuffer.isEmpty()) {
            for (LogEntry entry : logBuffer) {
                callback.onLogEntry(entry);
            }
            logBuffer.clear();
        }
    }

    public void connect(String url, boolean mockMode) {
        this.isMockMode = mockMode;

        if (mockMode) {
            // Start mock server and connect to it
            if (mockServer == null) {
                mockServer = new MockWebSocketServer();
            }
            mockServer.startServer();

            addLog(LogEntry.Type.SYSTEM, "Starting mock server...");
            webSocketManager.connect(mockServer.getServerUrl());
        } else {
            addLog(LogEntry.Type.SYSTEM, "Connecting to " + url);
            webSocketManager.connect(url);
        }
    }

    public void disconnect() {
        webSocketManager.disconnect();

        if (mockServer != null) {
            mockServer.stopServer();
            mockServer = null;
        }

        addLog(LogEntry.Type.SYSTEM, "Disconnected");
    }

    public WebSocketManager.ConnectionState getConnectionState() {
        return webSocketManager.getState();
    }

    public boolean isMockMode() {
        return isMockMode;
    }

    private void handleIncomingSms(String sender, String message) {
        Log.d(TAG, "Handling incoming SMS from " + sender);
        addLog(LogEntry.Type.INCOMING, "From " + sender + ": " + message);

        // Forward to backend via WebSocket
        webSocketManager.sendIncomingSms(sender, message);
    }

    private void handleSendSmsCommand(SendSmsCommand command) {
        if (command == null || command.getRecipients() == null || command.getRecipients().isEmpty()) {
            Log.w(TAG, "Invalid send SMS command");
            return;
        }

        List<String> recipients = command.getRecipients();
        String message = command.getMessage();
        String requestId = command.getRequestId();

        addLog(LogEntry.Type.SYSTEM, "Sending SMS to " + recipients.size() + " recipient(s)");

        smsSender.sendSms(recipients, message, (recipientList, successCount, failureCount) -> {
            addLog(LogEntry.Type.OUTGOING,
                    "Sent to " + successCount + "/" + recipientList.size() + " recipients");

            // Send confirmation to backend
            SmsSentConfirmation confirmation = new SmsSentConfirmation(
                    requestId, recipientList, successCount, failureCount);
            webSocketManager.sendSmsConfirmation(confirmation);
        });
    }

    private void addLog(LogEntry.Type type, String message) {
        LogEntry entry = new LogEntry(type, message);
        if (callback != null) {
            callback.onLogEntry(entry);
        } else {
            // Buffer the log if Activity isn't connected
            synchronized (logBuffer) {
                logBuffer.add(entry);
                // Keep buffer size manageable
                while (logBuffer.size() > MAX_LOG_BUFFER) {
                    logBuffer.remove(0);
                }
            }
        }
    }

    // WebSocketManager.WebSocketCallback implementation

    @Override
    public void onStateChanged(WebSocketManager.ConnectionState state) {
        Log.d(TAG, "Connection state changed: " + state);

        switch (state) {
            case CONNECTED:
                addLog(LogEntry.Type.SYSTEM, "Connected to server");
                break;
            case CONNECTING:
                addLog(LogEntry.Type.SYSTEM, "Connecting...");
                break;
            case DISCONNECTED:
                // Logged elsewhere
                break;
        }

        if (callback != null) {
            callback.onConnectionStateChanged(state);
        }

        // Update notification
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, createNotification());
        }
    }

    @Override
    public void onSendSmsCommand(SendSmsCommand command) {
        Log.d(TAG, "Received send SMS command");
        handleSendSmsCommand(command);
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "WebSocket error: " + error);
        addLog(LogEntry.Type.ERROR, error);
    }

    // Notification methods

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.notification_channel_description));

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE);

        String statusText;
        switch (webSocketManager.getState()) {
            case CONNECTED:
                statusText = "Connected" + (isMockMode ? " (Mock)" : "");
                break;
            case CONNECTING:
                statusText = "Connecting...";
                break;
            default:
                statusText = "Disconnected";
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(statusText)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }
}
