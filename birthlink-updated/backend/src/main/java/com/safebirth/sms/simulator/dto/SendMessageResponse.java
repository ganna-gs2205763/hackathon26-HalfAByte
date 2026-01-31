package com.safebirth.sms.simulator.dto;

/**
 * Response from sending a simulated SMS message.
 */
public record SendMessageResponse(
        ChatMessage userMessage,
        ChatMessage systemResponse
) {}
