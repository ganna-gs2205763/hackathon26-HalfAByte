package com.safebirth.api.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Dashboard statistics for NGO coordinators.
 * Provides comprehensive metrics for monitoring maternal care operations.
 */
@Builder
public record DashboardStatsDto(
        // Core counts
        long totalMothers,
        long totalVolunteers,
        long availableVolunteers,
        long activeRequests,
        long pendingRequests,
        long pendingEmergencies,
        long completedToday,
        long highRiskMothers,
        
        // Distribution maps
        Map<String, Long> mothersByZone,
        Map<String, Long> requestsByStatus,
        Map<String, Long> volunteersBySkill,
        
        // Upcoming due dates for planning
        List<DueDateCluster> upcomingDueDates
) {
    /**
     * Represents a cluster of due dates for dashboard visualization.
     */
    @Builder
    public record DueDateCluster(
            LocalDate date,
            long count
    ) {}
}
