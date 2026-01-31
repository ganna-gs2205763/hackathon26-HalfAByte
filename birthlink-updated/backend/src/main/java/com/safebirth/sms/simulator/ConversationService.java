package com.safebirth.sms.simulator;

import com.safebirth.sms.gateway.MockSmsGateway;
import com.safebirth.sms.simulator.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for managing simulated SMS conversations.
 * Stores conversation history in-memory and merges with MockSmsGateway outbox.
 */
@Service("simulatorConversationService")
public class ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);

    private final Map<String, List<ChatMessage>> conversations = new ConcurrentHashMap<>();
    private final MockSmsGateway mockSmsGateway;

    @Autowired
    public ConversationService(@Autowired(required = false) MockSmsGateway mockSmsGateway) {
        this.mockSmsGateway = mockSmsGateway;
    }

    /**
     * Record an inbound message (user sending SMS to system).
     */
    public ChatMessage recordInbound(String phone, String body) {
        ChatMessage message = ChatMessage.inbound(phone, body, LocalDateTime.now());
        conversations.computeIfAbsent(phone, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(message);
        log.debug("Recorded inbound message from {}: {}", phone, truncate(body));
        return message;
    }

    /**
     * Record an outbound message (system response to user).
     */
    public ChatMessage recordOutbound(String phone, String body) {
        ChatMessage message = ChatMessage.outbound(phone, body, LocalDateTime.now());
        conversations.computeIfAbsent(phone, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(message);
        log.debug("Recorded outbound message to {}: {}", phone, truncate(body));
        return message;
    }

    /**
     * Get the full conversation for a phone number.
     * Merges stored messages with any outbound messages from MockSmsGateway.
     */
    public ConversationDto getConversation(String phone) {
        List<ChatMessage> storedMessages = conversations.getOrDefault(phone, Collections.emptyList());

        // Get outbox messages from MockSmsGateway
        List<ChatMessage> allMessages = new ArrayList<>(storedMessages);

        if (mockSmsGateway != null) {
            List<MockSmsGateway.OutboxMessage> outboxMessages = mockSmsGateway.getMessagesSentTo(phone);

            // Convert outbox messages to ChatMessages, avoiding duplicates
            Set<String> existingBodies = storedMessages.stream()
                    .filter(m -> m.direction() == MessageDirection.OUTBOUND)
                    .map(ChatMessage::body)
                    .collect(Collectors.toSet());

            for (MockSmsGateway.OutboxMessage outbox : outboxMessages) {
                // Only add if not already in stored messages (avoid duplicates)
                if (!existingBodies.contains(outbox.message())) {
                    allMessages.add(ChatMessage.outbound(phone, outbox.message(), outbox.timestamp()));
                }
            }
        }

        // Sort by timestamp
        allMessages.sort(Comparator.comparing(ChatMessage::timestamp));

        return new ConversationDto(phone, allMessages);
    }

    /**
     * Get all known phone devices.
     */
    public List<DeviceDto> getDevices() {
        Map<String, DeviceDto> devices = new HashMap<>();

        // Add devices from conversations
        for (Map.Entry<String, List<ChatMessage>> entry : conversations.entrySet()) {
            String phone = entry.getKey();
            List<ChatMessage> messages = entry.getValue();

            LocalDateTime lastActivity = messages.isEmpty() ? LocalDateTime.now()
                    : messages.get(messages.size() - 1).timestamp();

            devices.put(phone, new DeviceDto(
                    phone,
                    generateLabel(phone),
                    messages.size(),
                    lastActivity));
        }

        // Add devices from outbox (may include phones we haven't seen yet)
        if (mockSmsGateway != null) {
            for (MockSmsGateway.OutboxMessage outbox : mockSmsGateway.getOutbox()) {
                String phone = outbox.to();
                if (!devices.containsKey(phone)) {
                    devices.put(phone, new DeviceDto(
                            phone,
                            generateLabel(phone),
                            1,
                            outbox.timestamp()));
                }
            }
        }

        return new ArrayList<>(devices.values());
    }

    /**
     * Get all outbox messages from MockSmsGateway.
     */
    public List<ChatMessage> getOutbox() {
        if (mockSmsGateway == null) {
            return Collections.emptyList();
        }

        return mockSmsGateway.getOutbox().stream()
                .map(o -> ChatMessage.outbound(o.to(), o.message(), o.timestamp()))
                .toList();
    }

    /**
     * Clear all conversations and the outbox.
     */
    public void reset() {
        conversations.clear();
        if (mockSmsGateway != null) {
            mockSmsGateway.clearOutbox();
        }
        log.info("Simulator reset: cleared all conversations and outbox");
    }

    private String generateLabel(String phone) {
        // Generate a friendly label based on last 4 digits
        if (phone == null || phone.length() < 4) {
            return "Phone";
        }
        return "Phone ..." + phone.substring(phone.length() - 4);
    }

    private String truncate(String text) {
        if (text == null)
            return "null";
        if (text.length() <= 50)
            return text;
        return text.substring(0, 47) + "...";
    }
}
