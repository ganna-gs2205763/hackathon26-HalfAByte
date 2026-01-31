package com.safebirth.api.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Dashboard statistics for NGO coordinators.
 * Provides comprehensive metrics for monitoring maternal care operations.
 */
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
    public record DueDateCluster(
            LocalDate date,
            long count
    ) {
        public static DueDateClusterBuilder builder() {
            return new DueDateClusterBuilder();
        }

        public static class DueDateClusterBuilder {
            private LocalDate date;
            private long count;

            public DueDateClusterBuilder date(LocalDate date) {
                this.date = date;
                return this;
            }

            public DueDateClusterBuilder count(long count) {
                this.count = count;
                return this;
            }

            public DueDateCluster build() {
                return new DueDateCluster(date, count);
            }
        }
    }

    public static DashboardStatsDtoBuilder builder() {
        return new DashboardStatsDtoBuilder();
    }

    public static class DashboardStatsDtoBuilder {
        private long totalMothers;
        private long totalVolunteers;
        private long availableVolunteers;
        private long activeRequests;
        private long pendingRequests;
        private long pendingEmergencies;
        private long completedToday;
        private long highRiskMothers;
        private Map<String, Long> mothersByZone;
        private Map<String, Long> requestsByStatus;
        private Map<String, Long> volunteersBySkill;
        private List<DueDateCluster> upcomingDueDates;

        public DashboardStatsDtoBuilder totalMothers(long totalMothers) {
            this.totalMothers = totalMothers;
            return this;
        }

        public DashboardStatsDtoBuilder totalVolunteers(long totalVolunteers) {
            this.totalVolunteers = totalVolunteers;
            return this;
        }

        public DashboardStatsDtoBuilder availableVolunteers(long availableVolunteers) {
            this.availableVolunteers = availableVolunteers;
            return this;
        }

        public DashboardStatsDtoBuilder activeRequests(long activeRequests) {
            this.activeRequests = activeRequests;
            return this;
        }

        public DashboardStatsDtoBuilder pendingRequests(long pendingRequests) {
            this.pendingRequests = pendingRequests;
            return this;
        }

        public DashboardStatsDtoBuilder pendingEmergencies(long pendingEmergencies) {
            this.pendingEmergencies = pendingEmergencies;
            return this;
        }

        public DashboardStatsDtoBuilder completedToday(long completedToday) {
            this.completedToday = completedToday;
            return this;
        }

        public DashboardStatsDtoBuilder highRiskMothers(long highRiskMothers) {
            this.highRiskMothers = highRiskMothers;
            return this;
        }

        public DashboardStatsDtoBuilder mothersByZone(Map<String, Long> mothersByZone) {
            this.mothersByZone = mothersByZone;
            return this;
        }

        public DashboardStatsDtoBuilder requestsByStatus(Map<String, Long> requestsByStatus) {
            this.requestsByStatus = requestsByStatus;
            return this;
        }

        public DashboardStatsDtoBuilder volunteersBySkill(Map<String, Long> volunteersBySkill) {
            this.volunteersBySkill = volunteersBySkill;
            return this;
        }

        public DashboardStatsDtoBuilder upcomingDueDates(List<DueDateCluster> upcomingDueDates) {
            this.upcomingDueDates = upcomingDueDates;
            return this;
        }

        public DashboardStatsDto build() {
            return new DashboardStatsDto(totalMothers, totalVolunteers, availableVolunteers,
                    activeRequests, pendingRequests, pendingEmergencies, completedToday, highRiskMothers,
                    mothersByZone, requestsByStatus, volunteersBySkill, upcomingDueDates);
        }
    }
}
