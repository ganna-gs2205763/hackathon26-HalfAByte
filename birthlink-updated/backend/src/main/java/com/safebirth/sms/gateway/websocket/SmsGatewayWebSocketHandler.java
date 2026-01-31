package com.safebirth.sms.gateway.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safebirth.sms.gateway.websocket.dto.IncomingSmsMessage;
import com.safebirth.sms.gateway.websocket.dto.SendSmsCommand;
import com.safebirth.sms.gateway.websocket.dto.SmsSentConfirmation;
import com.safebirth.sms.conversation.ConversationService;
import com.safebirth.sms.handler.SmsCommandHandler;
import com.safebirth.sms.parser.CommandType;
import com.safebirth.sms.parser.SmsCommand;
import com.safebirth.sms.parser.SmsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for SMS Gateway communication with Android app.
 * Handles incoming SMS messages, sends outbound SMS commands, and processes confirmations.
 *
 * Protocol:
 * - incoming_sms: App receives SMS on device, forwards to server
 * - send_sms: Server sends SMS command to app for delivery
 * - sms_sent: App confirms SMS was sent
 * - ping/pong: Keep-alive mechanism
 */
@Component
public class SmsGatewayWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(SmsGatewayWebSocketHandler.class);

    private final ObjectMapper objectMapper;
    private final SmsParser smsParser;
    private final SmsCommandHandler smsCommandHandler;
    private final ConversationService conversationService;

    public SmsGatewayWebSocketHandler(
            ObjectMapper objectMapper,
            SmsParser smsParser,
            SmsCommandHandler smsCommandHandler,
            @Qualifier("aiConversationManager") ConversationService conversationService) {
        this.objectMapper = objectMapper;
        this.smsParser = smsParser;
        this.smsCommandHandler = smsCommandHandler;
        this.conversationService = conversationService;
    }

    // Track connected sessions (in POC, we expect single Android app connection)
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // Track pending requests for confirmation (optional for POC)
    private final Map<String, PendingRequest> pendingRequests = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        log.info("📱 SMS Gateway connected: sessionId={}, remoteAddr={}",
                sessionId, session.getRemoteAddress());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        log.info("📱 SMS Gateway disconnected: sessionId={}, status={}", sessionId, status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("📥 WS received: {}", truncateForLog(payload));

        try {
            JsonNode json = objectMapper.readTree(payload);
            String type = json.has("type") ? json.get("type").asText() : null;

            if (type == null) {
                log.warn("Received message without type field: {}", truncateForLog(payload));
                return;
            }

            switch (type) {
                case IncomingSmsMessage.TYPE -> handleIncomingSms(session, json);
                case SmsSentConfirmation.TYPE -> handleSmsSentConfirmation(json);
                case "ping" -> handlePing(session);
                default -> log.warn("Unknown message type: {}", type);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse WebSocket message: {}", e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        sessions.remove(session.getId());
    }

    /**
     * Handle incoming SMS from Android app.
     * Uses AI for natural conversations, but falls back to command handler
     * for critical operations (EMERGENCY, SUPPORT, ACCEPT, COMPLETE) that require
     * location matching and volunteer notifications.
     */
    private void handleIncomingSms(WebSocketSession session, JsonNode json) {
        try {
            IncomingSmsMessage incoming = objectMapper.treeToValue(json, IncomingSmsMessage.class);

            if (!incoming.isValid()) {
                log.warn("Invalid incoming SMS message: {}", json);
                return;
            }

            log.info("📨 Incoming SMS via WebSocket: from={}, message={}",
                    maskPhone(incoming.sender()), truncateForLog(incoming.message()));

            String response;

            // Parse command to check if it needs location matching
            SmsCommand command = smsParser.parse(incoming.sender(), incoming.message());

            // Use old command handler for operations that require location matching
            if (requiresLocationMatching(command.type())) {
                log.info("🎯 Using command handler for {}", command.type());
                response = smsCommandHandler.handle(command);
            } else {
                // Use AI conversation service for everything else
                log.info("🤖 Using AI conversation service");
                response = conversationService.processMessage(incoming.sender(), incoming.message());
            }

            // Send response back via WebSocket for the app to deliver as SMS
            String requestId = "WS-" + UUID.randomUUID().toString().substring(0, 8);
            SendSmsCommand sendCmd = SendSmsCommand.create(requestId, incoming.sender(), response);

            sendToSession(session, sendCmd);
            log.info("📤 Response sent via WebSocket: requestId={}, to={}",
                    requestId, maskPhone(incoming.sender()));

        } catch (Exception e) {
            log.error("Error handling incoming SMS: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if this command type requires location-based volunteer matching.
     * These commands use the old handler to ensure proper matching and notifications.
     */
    private boolean requiresLocationMatching(CommandType type) {
        return type == CommandType.EMERGENCY ||
               type == CommandType.SUPPORT ||
               type == CommandType.ACCEPT_CASE ||
               type == CommandType.COMPLETE_CASE ||
               type == CommandType.CANCEL_CASE;
    }

    /**
     * Handle SMS sent confirmation from Android app.
     * Logs the result and can be extended for retry logic.
     */
    private void handleSmsSentConfirmation(JsonNode json) {
        try {
            SmsSentConfirmation confirmation = objectMapper.treeToValue(json, SmsSentConfirmation.class);

            if (!confirmation.isValid()) {
                log.warn("Invalid SMS sent confirmation: {}", json);
                return;
            }

            // Remove from pending requests
            pendingRequests.remove(confirmation.requestId());

            if (confirmation.isSuccess()) {
                log.info("✅ SMS delivery confirmed: requestId={}, successCount={}",
                        confirmation.requestId(), confirmation.successCount());
            } else {
                log.warn("❌ SMS delivery failed: requestId={}, failureCount={}",
                        confirmation.requestId(), confirmation.failureCount());
            }
        } catch (Exception e) {
            log.error("Error handling SMS confirmation: {}", e.getMessage());
        }
    }

    /**
     * Handle ping message, respond with pong.
     */
    private void handlePing(WebSocketSession session) {
        try {
            String pong = objectMapper.writeValueAsString(Map.of("type", "pong"));
            session.sendMessage(new TextMessage(pong));
            log.debug("🏓 Pong sent to session {}", session.getId());
        } catch (IOException e) {
            log.error("Failed to send pong: {}", e.getMessage());
        }
    }

    /**
     * Send an SMS via the connected Android app.
     * Returns the request ID for tracking.
     */
    public String sendSms(String to, String message) {
        return sendSms(List.of(to), message);
    }

    /**
     * Send an SMS to multiple recipients via the connected Android app.
     * Returns the request ID for tracking.
     */
    public String sendSms(List<String> recipients, String message) {
        if (sessions.isEmpty()) {
            log.warn("No SMS Gateway connected, cannot send SMS");
            return null;
        }

        String requestId = "WS-" + UUID.randomUUID().toString().substring(0, 8);
        SendSmsCommand cmd = SendSmsCommand.create(requestId, recipients, message);

        // Track pending request
        pendingRequests.put(requestId, new PendingRequest(requestId, recipients, message, System.currentTimeMillis()));

        // Send to all connected sessions (typically just one Android app)
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                sendToSession(session, cmd);
            }
        }

        log.info("📤 SMS command sent via WebSocket: requestId={}, recipients={}",
                requestId, recipients.size());
        return requestId;
    }

    /**
     * Check if any Android app is connected.
     */
    public boolean isConnected() {
        return sessions.values().stream().anyMatch(WebSocketSession::isOpen);
    }

    /**
     * Get count of connected sessions.
     */
    public int getConnectedCount() {
        return (int) sessions.values().stream().filter(WebSocketSession::isOpen).count();
    }

    private void sendToSession(WebSocketSession session, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            session.sendMessage(new TextMessage(json));
            log.debug("📤 WS sent: {}", truncateForLog(json));
        } catch (IOException e) {
            log.error("Failed to send WebSocket message: {}", e.getMessage());
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, phone.length() - 4) + "****";
    }

    private String truncateForLog(String text) {
        if (text == null) return "null";
        if (text.length() <= 200) return text;
        return text.substring(0, 197) + "...";
    }

    /**
     * Record for tracking pending SMS requests.
     */
    private record PendingRequest(
            String requestId,
            List<String> recipients,
            String message,
            long timestamp
    ) {}
}
