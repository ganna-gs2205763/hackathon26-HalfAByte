package com.safebirth.sms.simulator.dto;

import java.util.List;

/**
 * Full conversation for a phone number.
 */
public record ConversationDto(
        String phoneNumber,
        List<ChatMessage> messages
) {}
