package com.safebirth.sms.simulator.dto;

import java.time.LocalDateTime;

/**
 * Represents a simulated phone device.
 */
public record DeviceDto(
        String phoneNumber,
        String label,
        int messageCount,
        LocalDateTime lastActivity
) {}
