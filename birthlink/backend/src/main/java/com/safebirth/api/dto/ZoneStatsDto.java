package com.safebirth.api.dto;

import lombok.Builder;

/**
 * Zone-level statistics for the dashboard.
 */
@Builder
public record ZoneStatsDto(
        String zone,
        long motherCount,
        long volunteerCount,
        long availableVolunteers,
        long activeRequests,
        long pendingEmergencies
) {}
