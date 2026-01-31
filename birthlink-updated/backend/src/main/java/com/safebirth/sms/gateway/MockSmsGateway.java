package com.safebirth.sms.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Mock SMS gateway for development and testing.
 * Logs messages with emojis instead of actually sending them.
 * Maintains an in-memory outbox for test assertions.
 * Activated when twilio.mock-enabled is true (default for dev).
 */
@Component
@ConditionalOnProperty(name = "twilio.mock-enabled", havingValue = "true", matchIfMissing = true)
public class MockSmsGateway implements SmsGateway {

    private static final Logger log = LoggerFactory.getLogger(MockSmsGateway.class);

    private final List<OutboxMessage> outbox = Collections.synchronizedList(new ArrayList<>());

    @Override
    public String sendSms(String to, String message) {
        String mockSid = "MOCK-" + UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime timestamp = LocalDateTime.now();

        // Log for visual clarity in dev mode
        log.info("SMS OUTBOUND [MOCK MODE] - To: {}, Message: {}, SID: {}, Time: {}",
                to, truncateForLog(message), mockSid, timestamp);

        outbox.add(new OutboxMessage(to, message, mockSid, timestamp));

        return mockSid;
    }

    @Override
    public String generateTwimlResponse(String message) {
        String twiml = String.format("""
                <?xml version="1.0" encoding="UTF-8"?>
                <Response>
                    <Message>%s</Message>
                </Response>
                """, escapeXml(message));

        log.debug("Generated TwiML response for message: {}", truncateForLog(message));
        return twiml;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    /**
     * Get all messages in the outbox.
     * Useful for testing verification.
     *
     * @return unmodifiable list of outbox messages
     */
    public List<OutboxMessage> getOutbox() {
        return new ArrayList<>(outbox);
    }

    /**
     * Get the last sent message.
     *
     * @return the last message or null if outbox is empty
     */
    public OutboxMessage getLastMessage() {
        if (outbox.isEmpty()) {
            return null;
        }
        return outbox.get(outbox.size() - 1);
    }

    /**
     * Get messages sent to a specific phone number.
     *
     * @param phoneNumber the recipient phone number
     * @return list of messages sent to this number
     */
    public List<OutboxMessage> getMessagesSentTo(String phoneNumber) {
        return outbox.stream()
                .filter(msg -> msg.to().equals(phoneNumber))
                .toList();
    }

    /**
     * Get the count of messages in the outbox.
     *
     * @return outbox size
     */
    public int getOutboxSize() {
        return outbox.size();
    }

    /**
     * Clear all messages from the outbox.
     * Useful for test cleanup between test cases.
     */
    public void clearOutbox() {
        outbox.clear();
        log.debug("Mock SMS outbox cleared");
    }

    /**
     * Check if a message containing specific text was sent.
     *
     * @param textFragment the text to search for
     * @return true if any message contains this text
     */
    public boolean hasMessageContaining(String textFragment) {
        return outbox.stream()
                .anyMatch(msg -> msg.message().contains(textFragment));
    }

    /**
     * Check if a message was sent to a specific number.
     *
     * @param phoneNumber the phone number to check
     * @return true if any message was sent to this number
     */
    public boolean hasMessageTo(String phoneNumber) {
        return outbox.stream()
                .anyMatch(msg -> msg.to().equals(phoneNumber));
    }

    private String truncateForLog(String text) {
        if (text == null) return "null";
        if (text.length() <= 100) return text;
        return text.substring(0, 97) + "...";
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /**
     * Record representing a message in the outbox.
     * Immutable data structure for test assertions.
     */
    public record OutboxMessage(
            String to,
            String message,
            String sid,
            LocalDateTime timestamp
    ) {}
}
