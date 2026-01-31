package com.safebirth.sms.gateway;

import com.safebirth.sms.gateway.websocket.SmsGatewayWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * WebSocket SMS Gateway service for sending SMS via connected Android app.
 * This is a parallel service to MockSmsGateway - both can work simultaneously.
 *
 * Use this gateway when you want to send real SMS via a connected Android device.
 * Falls back gracefully if no device is connected.
 *
 * Note: This does NOT implement SmsGateway interface intentionally.
 * It's a parallel/supplementary service, not a replacement.
 */
@Service
@ConditionalOnProperty(name = "sms.gateway.websocket.enabled", havingValue = "true", matchIfMissing = true)
public class WebSocketSmsGateway {

    private static final Logger log = LoggerFactory.getLogger(WebSocketSmsGateway.class);

    private final SmsGatewayWebSocketHandler webSocketHandler;

    public WebSocketSmsGateway(SmsGatewayWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    /**
     * Check if an Android SMS Gateway app is connected.
     *
     * @return true if at least one device is connected via WebSocket
     */
    public boolean isConnected() {
        return webSocketHandler.isConnected();
    }

    /**
     * Get the number of connected SMS Gateway devices.
     *
     * @return count of connected WebSocket sessions
     */
    public int getConnectedCount() {
        return webSocketHandler.getConnectedCount();
    }

    /**
     * Send an SMS to a single recipient via the connected Android app.
     *
     * @param to      the recipient phone number (E.164 format)
     * @param message the message body
     * @return the request ID if sent, null if no device connected
     */
    public String sendSms(String to, String message) {
        if (!isConnected()) {
            log.warn("WebSocket SMS Gateway: No device connected, cannot send SMS to {}", maskPhone(to));
            return null;
        }

        log.info("WebSocket SMS Gateway: Sending SMS to {}", maskPhone(to));
        return webSocketHandler.sendSms(to, message);
    }

    /**
     * Send an SMS to multiple recipients via the connected Android app.
     *
     * @param recipients list of recipient phone numbers
     * @param message    the message body
     * @return the request ID if sent, null if no device connected
     */
    public String sendSms(List<String> recipients, String message) {
        if (!isConnected()) {
            log.warn("WebSocket SMS Gateway: No device connected, cannot send SMS to {} recipients",
                    recipients.size());
            return null;
        }

        log.info("WebSocket SMS Gateway: Sending SMS to {} recipients", recipients.size());
        return webSocketHandler.sendSms(recipients, message);
    }

    /**
     * Send SMS and return success status.
     * Convenience method that returns boolean instead of request ID.
     *
     * @param to      the recipient phone number
     * @param message the message body
     * @return true if message was queued for sending
     */
    public boolean trySendSms(String to, String message) {
        return sendSms(to, message) != null;
    }

    /**
     * Send SMS to multiple recipients and return success status.
     *
     * @param recipients list of recipient phone numbers
     * @param message    the message body
     * @return true if message was queued for sending
     */
    public boolean trySendSms(List<String> recipients, String message) {
        return sendSms(recipients, message) != null;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, phone.length() - 4) + "****";
    }
}
