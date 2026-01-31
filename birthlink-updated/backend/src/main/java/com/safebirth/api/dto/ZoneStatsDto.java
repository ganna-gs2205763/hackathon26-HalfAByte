package com.safebirth.api.dto;

/**
 * Zone-level statistics for the dashboard.
 */
public record ZoneStatsDto(
        String zone,
        long motherCount,
        long volunteerCount,
        long availableVolunteers,
        long activeRequests,
        long pendingEmergencies
) {
    public static ZoneStatsDtoBuilder builder() {
        return new ZoneStatsDtoBuilder();
    }

    public static class ZoneStatsDtoBuilder {
        private String zone;
        private long motherCount;
        private long volunteerCount;
        private long availableVolunteers;
        private long activeRequests;
        private long pendingEmergencies;

        public ZoneStatsDtoBuilder zone(String zone) {
            this.zone = zone;
            return this;
        }

        public ZoneStatsDtoBuilder motherCount(long motherCount) {
            this.motherCount = motherCount;
            return this;
        }

        public ZoneStatsDtoBuilder volunteerCount(long volunteerCount) {
            this.volunteerCount = volunteerCount;
            return this;
        }

        public ZoneStatsDtoBuilder availableVolunteers(long availableVolunteers) {
            this.availableVolunteers = availableVolunteers;
            return this;
        }

        public ZoneStatsDtoBuilder activeRequests(long activeRequests) {
            this.activeRequests = activeRequests;
            return this;
        }

        public ZoneStatsDtoBuilder pendingEmergencies(long pendingEmergencies) {
            this.pendingEmergencies = pendingEmergencies;
            return this;
        }

        public ZoneStatsDto build() {
            return new ZoneStatsDto(zone, motherCount, volunteerCount, availableVolunteers, activeRequests, pendingEmergencies);
        }
    }
}
