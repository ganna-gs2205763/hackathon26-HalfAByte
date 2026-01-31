package com.safebirth.sms.simulator.dto;

import java.time.LocalDateTime;

/**
 * Represents a single message in a conversation.
 */
public record ChatMessage(
        String id,
        String phoneNumber,
        String body,
        MessageDirection direction,
        LocalDateTime timestamp
) {
    public static ChatMessage inbound(String phone, String body, LocalDateTime timestamp) {
        return new ChatMessage(
                "IN-" + System.nanoTime(),
                phone,
                body,
                MessageDirection.INBOUND,
                timestamp
        );
    }

    public static ChatMessage outbound(String phone, String body, LocalDateTime timestamp) {
        return new ChatMessage(
                "OUT-" + System.nanoTime(),
                phone,
                body,
                MessageDirection.OUTBOUND,
                timestamp
        );
    }
}
