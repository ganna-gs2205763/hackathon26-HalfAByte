package com.halfabyte.smsgateway;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

/**
 * Mock WebSocket server for testing SMS functionality without a real backend.
 * Runs locally on the device and echoes incoming SMS as send commands.
 */
public class MockWebSocketServer extends NanoWSD {
    private static final String TAG = "MockWebSocketServer";
    private static final int PORT = 8080;

    private final Gson gson;
    private volatile boolean isRunning = false;

    public MockWebSocketServer() {
        super(PORT);
        this.gson = new Gson();
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        Log.d(TAG, "New WebSocket connection from: " + handshake.getRemoteIpAddress());
        return new MockWebSocket(handshake);
    }

    public void startServer() {
        if (isRunning) {
            Log.d(TAG, "Server already running");
            return;
        }

        try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            isRunning = true;
            Log.d(TAG, "Mock WebSocket server started on port " + PORT);
        } catch (IOException e) {
            Log.e(TAG, "Failed to start mock server", e);
        }
    }

    public void stopServer() {
        if (!isRunning) {
            return;
        }

        try {
            stop();
            isRunning = false;
            Log.d(TAG, "Mock WebSocket server stopped");
        } catch (Exception e) {
            Log.e(TAG, "Error stopping server", e);
        }
    }

    public String getServerUrl() {
        return "ws://127.0.0.1:" + PORT;
    }

    public boolean isServerRunning() {
        return isRunning && isAlive();
    }

    /**
     * Inner class representing a WebSocket connection.
     */
    private class MockWebSocket extends NanoWSD.WebSocket {

        public MockWebSocket(IHTTPSession handshakeRequest) {
            super(handshakeRequest);
        }

        @Override
        protected void onOpen() {
            Log.d(TAG, "Mock WebSocket opened");
        }

        @Override
        protected void onClose(NanoWSD.WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
            Log.d(TAG, "Mock WebSocket closed: " + code + " - " + reason);
        }

        @Override
        protected void onMessage(NanoWSD.WebSocketFrame message) {
            try {
                String payload = message.getTextPayload();
                if (payload == null || payload.isEmpty()) {
                    Log.w(TAG, "Received empty message");
                    return;
                }

                Log.d(TAG, "Mock server received: " + payload);

                // Parse the incoming message
                Map<String, Object> msgMap = gson.fromJson(payload, HashMap.class);
                if (msgMap == null) {
                    Log.w(TAG, "Failed to parse message as JSON");
                    return;
                }

                String type = (String) msgMap.get("type");

                if ("incoming_sms".equals(type)) {
                    // Echo back as a send_sms command (for testing)
                    String sender = (String) msgMap.get("sender");
                    String originalMessage = (String) msgMap.get("message");

                    if (sender == null || sender.isEmpty()) {
                        Log.w(TAG, "No sender in incoming SMS");
                        return;
                    }

                    // Create a mock response - send back to the original sender
                    Map<String, Object> response = new HashMap<>();
                    response.put("type", "send_sms");
                    response.put("request_id", UUID.randomUUID().toString());
                    response.put("recipients", Arrays.asList(sender));
                    response.put("message", "[AUTO-REPLY] Got your message: " +
                            (originalMessage != null
                                    ? originalMessage.substring(0, Math.min(50, originalMessage.length()))
                                    : ""));

                    String responseJson = gson.toJson(response);
                    Log.d(TAG, "Mock server sending response: " + responseJson);

                    // Send response after a small delay
                    new Thread(() -> {
                        try {
                            Thread.sleep(300);
                            send(responseJson);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to send mock response", e);
                        }
                    }).start();

                } else if ("sms_sent".equals(type)) {
                    Log.d(TAG, "Mock server received SMS sent confirmation");
                } else {
                    Log.d(TAG, "Mock server received unknown type: " + type);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing message in mock server", e);
            }
        }

        @Override
        protected void onPong(NanoWSD.WebSocketFrame pong) {
            Log.d(TAG, "Received pong");
        }

        @Override
        protected void onException(IOException exception) {
            Log.e(TAG, "Mock WebSocket exception: " + exception.getMessage());
        }
    }
}
