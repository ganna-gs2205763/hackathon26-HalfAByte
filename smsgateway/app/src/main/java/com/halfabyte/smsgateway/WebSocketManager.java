package com.halfabyte.smsgateway;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.halfabyte.smsgateway.models.IncomingSmsMessage;
import com.halfabyte.smsgateway.models.SendSmsCommand;
import com.halfabyte.smsgateway.models.SmsSentConfirmation;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Manages WebSocket connection to the backend server.
 * Handles connection, reconnection, and message parsing.
 */
public class WebSocketManager extends WebSocketListener {
    private static final String TAG = "WebSocketManager";
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private static final long INITIAL_RECONNECT_DELAY = 1000; // 1 second
    private static final long MAX_RECONNECT_DELAY = 30000; // 30 seconds

    public enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    public interface WebSocketCallback {
        void onStateChanged(ConnectionState state);

        void onSendSmsCommand(SendSmsCommand command);

        void onError(String error);
    }

    private final OkHttpClient client;
    private final Gson gson;
    private final Handler mainHandler;
    private final WebSocketCallback callback;

    private WebSocket webSocket;
    private String serverUrl;
    private ConnectionState state = ConnectionState.DISCONNECTED;
    private boolean shouldReconnect = false;
    private long reconnectDelay = INITIAL_RECONNECT_DELAY;
    private Runnable reconnectRunnable;

    public WebSocketManager(WebSocketCallback callback) {
        this.callback = callback;
        this.gson = new Gson();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.client = new OkHttpClient.Builder()
                .pingInterval(30, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS) // No timeout for WebSocket
                .build();
    }

    /**
     * Connect to the WebSocket server.
     * 
     * @param url The WebSocket server URL (ws:// or wss://)
     */
    public void connect(String url) {
        if (state == ConnectionState.CONNECTING || state == ConnectionState.CONNECTED) {
            Log.d(TAG, "Already connected or connecting");
            return;
        }

        this.serverUrl = url;
        this.shouldReconnect = true;
        this.reconnectDelay = INITIAL_RECONNECT_DELAY;

        setState(ConnectionState.CONNECTING);

        Request request = new Request.Builder()
                .url(url)
                .build();

        webSocket = client.newWebSocket(request, this);
    }

    /**
     * Disconnect from the WebSocket server.
     */
    public void disconnect() {
        shouldReconnect = false;
        cancelReconnect();

        if (webSocket != null) {
            webSocket.close(NORMAL_CLOSURE_STATUS, "User disconnected");
            webSocket = null;
        }

        setState(ConnectionState.DISCONNECTED);
    }

    /**
     * Send an incoming SMS message to the backend.
     */
    public void sendIncomingSms(String sender, String message) {
        if (state != ConnectionState.CONNECTED || webSocket == null) {
            Log.w(TAG, "Cannot send message - not connected");
            return;
        }

        IncomingSmsMessage smsMessage = new IncomingSmsMessage(sender, message);
        String json = gson.toJson(smsMessage);
        webSocket.send(json);
        Log.d(TAG, "Sent incoming SMS to backend: " + json);
    }

    /**
     * Send SMS sent confirmation to the backend.
     */
    public void sendSmsConfirmation(SmsSentConfirmation confirmation) {
        if (state != ConnectionState.CONNECTED || webSocket == null) {
            Log.w(TAG, "Cannot send confirmation - not connected");
            return;
        }

        String json = gson.toJson(confirmation);
        webSocket.send(json);
        Log.d(TAG, "Sent SMS confirmation to backend: " + json);
    }

    public ConnectionState getState() {
        return state;
    }

    // WebSocketListener callbacks

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Log.d(TAG, "WebSocket connected");
        reconnectDelay = INITIAL_RECONNECT_DELAY;
        mainHandler.post(() -> setState(ConnectionState.CONNECTED));
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.d(TAG, "Received message: " + text);

        try {
            JsonObject json = JsonParser.parseString(text).getAsJsonObject();
            String type = json.has("type") ? json.get("type").getAsString() : "";

            if ("send_sms".equals(type)) {
                SendSmsCommand command = gson.fromJson(text, SendSmsCommand.class);
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onSendSmsCommand(command);
                    }
                });
            } else {
                Log.w(TAG, "Unknown message type: " + type);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse message", e);
            mainHandler.post(() -> {
                if (callback != null) {
                    callback.onError("Failed to parse message: " + e.getMessage());
                }
            });
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        Log.d(TAG, "WebSocket closing: " + code + " - " + reason);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        Log.d(TAG, "WebSocket closed: " + code + " - " + reason);
        mainHandler.post(() -> {
            setState(ConnectionState.DISCONNECTED);
            if (shouldReconnect) {
                scheduleReconnect();
            }
        });
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.e(TAG, "WebSocket failure", t);
        mainHandler.post(() -> {
            setState(ConnectionState.DISCONNECTED);
            if (callback != null) {
                callback.onError("Connection failed: " + t.getMessage());
            }
            if (shouldReconnect) {
                scheduleReconnect();
            }
        });
    }

    // Private methods

    private void setState(ConnectionState newState) {
        if (state != newState) {
            state = newState;
            if (callback != null) {
                callback.onStateChanged(newState);
            }
        }
    }

    private void scheduleReconnect() {
        cancelReconnect();

        Log.d(TAG, "Scheduling reconnect in " + reconnectDelay + "ms");

        reconnectRunnable = () -> {
            if (shouldReconnect && serverUrl != null) {
                Log.d(TAG, "Attempting to reconnect...");
                setState(ConnectionState.CONNECTING);

                Request request = new Request.Builder()
                        .url(serverUrl)
                        .build();

                webSocket = client.newWebSocket(request, this);
            }
        };

        mainHandler.postDelayed(reconnectRunnable, reconnectDelay);

        // Exponential backoff
        reconnectDelay = Math.min(reconnectDelay * 2, MAX_RECONNECT_DELAY);
    }

    private void cancelReconnect() {
        if (reconnectRunnable != null) {
            mainHandler.removeCallbacks(reconnectRunnable);
            reconnectRunnable = null;
        }
    }
}
