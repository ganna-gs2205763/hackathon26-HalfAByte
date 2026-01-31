package com.safebirth.sms.simulator.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to send a simulated SMS message.
 */
public record SendMessageRequest(
        @NotBlank(message = "Phone number is required")
        String phoneNumber,

        @NotBlank(message = "Message body is required")
        String body
) {}
