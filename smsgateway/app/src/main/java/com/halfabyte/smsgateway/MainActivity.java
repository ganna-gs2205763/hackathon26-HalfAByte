package com.halfabyte.smsgateway;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halfabyte.smsgateway.models.LogEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity with UI for controlling the SMS Gateway.
 */
public class MainActivity extends AppCompatActivity implements SmsGatewayService.ServiceCallback {
    private static final String PREFS_NAME = "sms_gateway_prefs";

    // UI Components
    private TextView websocketUrlDisplay;
    private Button connectButton;
    private View statusIndicator;
    private TextView statusText;
    private TextView connectionModeText;
    private RecyclerView logRecyclerView;
    private TextView clearLogButton;

    // Service
    private SmsGatewayService gatewayService;
    private boolean serviceBound = false;

    // Adapter
    private LogAdapter logAdapter;

    // Permission launcher
    private ActivityResultLauncher<String[]> permissionLauncher;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SmsGatewayService.LocalBinder binder = (SmsGatewayService.LocalBinder) service;
            gatewayService = binder.getService();
            gatewayService.setCallback(MainActivity.this);
            serviceBound = true;

            // Update UI with current state
            updateConnectionUI(gatewayService.getConnectionState());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            gatewayService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        loadPreferences();
        setupPermissionLauncher();

        // Start and bind to service
        startGatewayService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindToService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindFromService();
    }

    private void initViews() {
        websocketUrlDisplay = findViewById(R.id.websocketUrlDisplay);
        connectButton = findViewById(R.id.connectButton);
        statusIndicator = findViewById(R.id.statusIndicator);
        statusText = findViewById(R.id.statusText);
        connectionModeText = findViewById(R.id.connectionModeText);
        logRecyclerView = findViewById(R.id.logRecyclerView);
        clearLogButton = findViewById(R.id.clearLogButton);

        // Setup RecyclerView
        logAdapter = new LogAdapter(this);
        logRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        logRecyclerView.setAdapter(logAdapter);
    }

    private void setupListeners() {
        connectButton.setOnClickListener(v -> onConnectButtonClicked());
        clearLogButton.setOnClickListener(v -> logAdapter.clear());
    }

    private void loadPreferences() {
        // URL is now hardcoded in strings.xml
        connectionModeText.setText("LIVE");
    }

    private void savePreferences() {
        // No preferences to save - URL is hardcoded
    }

    private void setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                results -> {
                    boolean allGranted = true;
                    for (Boolean granted : results.values()) {
                        if (!granted) {
                            allGranted = false;
                            break;
                        }
                    }

                    if (allGranted) {
                        logAdapter.addEntry(new LogEntry(LogEntry.Type.SYSTEM, "Permissions granted"));
                        performConnect();
                    } else {
                        Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
                        logAdapter.addEntry(new LogEntry(LogEntry.Type.ERROR, "Permissions denied"));
                    }
                });
    }

    private void onConnectButtonClicked() {
        if (!serviceBound || gatewayService == null) {
            Toast.makeText(this, "Service not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        WebSocketManager.ConnectionState state = gatewayService.getConnectionState();

        if (state == WebSocketManager.ConnectionState.CONNECTED ||
                state == WebSocketManager.ConnectionState.CONNECTING) {
            // Disconnect
            gatewayService.disconnect();
        } else {
            // Check permissions first
            if (checkAndRequestPermissions()) {
                performConnect();
            }
        }
    }

    private boolean checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.RECEIVE_SMS);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.SEND_SMS);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            permissionLauncher.launch(permissionsNeeded.toArray(new String[0]));
            return false;
        }

        return true;
    }

    private void performConnect() {
        // Use hardcoded URL from strings.xml
        String url = getString(R.string.backend_websocket_url);
        gatewayService.connect(url, false);
    }

    private void startGatewayService() {
        Intent intent = new Intent(this, SmsGatewayService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void bindToService() {
        Intent intent = new Intent(this, SmsGatewayService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindFromService() {
        if (serviceBound) {
            if (gatewayService != null) {
                gatewayService.setCallback(null);
            }
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    private void updateConnectionUI(WebSocketManager.ConnectionState state) {
        int indicatorColor;
        String statusString;
        String buttonText;
        boolean inputsEnabled;

        switch (state) {
            case CONNECTED:
                indicatorColor = R.color.status_connected;
                statusString = getString(R.string.status_connected);
                buttonText = getString(R.string.disconnect);
                inputsEnabled = false;
                break;
            case CONNECTING:
                indicatorColor = R.color.status_connecting;
                statusString = getString(R.string.status_connecting);
                buttonText = getString(R.string.disconnect);
                inputsEnabled = false;
                break;
            case DISCONNECTED:
            default:
                indicatorColor = R.color.status_disconnected;
                statusString = getString(R.string.status_disconnected);
                buttonText = getString(R.string.connect);
                inputsEnabled = true;
                break;
        }

        // Update status indicator color
        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.status_indicator);
        if (drawable != null) {
            drawable = (GradientDrawable) drawable.mutate();
            drawable.setColor(ContextCompat.getColor(this, indicatorColor));
            statusIndicator.setBackground(drawable);
        }

        statusText.setText(statusString);
        connectButton.setText(buttonText);

        // URL display doesn't need enable/disable
    }

    // SmsGatewayService.ServiceCallback implementation

    @Override
    public void onLogEntry(LogEntry entry) {
        runOnUiThread(() -> {
            logAdapter.addEntry(entry);
            logRecyclerView.smoothScrollToPosition(0);
        });
    }

    @Override
    public void onConnectionStateChanged(WebSocketManager.ConnectionState state) {
        runOnUiThread(() -> updateConnectionUI(state));
    }
}
