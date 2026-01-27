package com.safebirth.api;

import com.safebirth.api.dto.CaseDto;
import com.safebirth.api.dto.DashboardStatsDto;
import com.safebirth.api.dto.VolunteerDto;
import com.safebirth.api.dto.ZoneStatsDto;
import com.safebirth.domain.helprequest.HelpRequest;
import com.safebirth.domain.helprequest.HelpRequestRepository;
import com.safebirth.domain.helprequest.RequestStatus;
import com.safebirth.domain.helprequest.RequestType;
import com.safebirth.domain.mother.Mother;
import com.safebirth.domain.mother.MotherRepository;
import com.safebirth.domain.mother.RiskLevel;
import com.safebirth.domain.volunteer.AvailabilityStatus;
import com.safebirth.domain.volunteer.SkillType;
import com.safebirth.domain.volunteer.Volunteer;
import com.safebirth.domain.volunteer.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for dashboard statistics and data aggregation.
 * Provides comprehensive metrics for NGO coordinators.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final MotherRepository motherRepository;
    private final VolunteerRepository volunteerRepository;
    private final HelpRequestRepository helpRequestRepository;

    /**
     * Get comprehensive dashboard statistics.
     *
     * @return dashboard statistics DTO
     */
    public DashboardStatsDto getStats() {
        log.debug("Computing dashboard statistics");

        List<Mother> allMothers = motherRepository.findAll();
        List<Volunteer> allVolunteers = volunteerRepository.findAll();
        List<HelpRequest> allRequests = helpRequestRepository.findAll();

        // Calculate active requests (ACCEPTED + IN_PROGRESS)
        long activeRequests = allRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.ACCEPTED || 
                            r.getStatus() == RequestStatus.IN_PROGRESS)
                .count();

        // Calculate pending requests
        long pendingRequests = allRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.PENDING)
                .count();

        // Calculate pending emergencies
        long pendingEmergencies = allRequests.stream()
                .filter(r -> r.getRequestType() == RequestType.EMERGENCY && 
                            r.getStatus() == RequestStatus.PENDING)
                .count();

        // Calculate completed today
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long completedToday = allRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.COMPLETED &&
                            r.getClosedAt() != null &&
                            r.getClosedAt().isAfter(todayStart))
                .count();

        // Calculate high-risk mothers
        long highRiskMothers = allMothers.stream()
                .filter(m -> m.getRiskLevel() == RiskLevel.HIGH)
                .count();

        return DashboardStatsDto.builder()
                .totalMothers(allMothers.size())
                .totalVolunteers(allVolunteers.size())
                .availableVolunteers(volunteerRepository.countByAvailability(AvailabilityStatus.AVAILABLE))
                .activeRequests(activeRequests)
                .pendingRequests(pendingRequests)
                .pendingEmergencies(pendingEmergencies)
                .completedToday(completedToday)
                .highRiskMothers(highRiskMothers)
                .mothersByZone(getMothersByZone(allMothers))
                .requestsByStatus(getRequestsByStatus(allRequests))
                .volunteersBySkill(getVolunteersBySkill(allVolunteers))
                .upcomingDueDates(getUpcomingDueDates(allMothers))
                .build();
    }

    /**
     * Get zone-level statistics for all zones.
     *
     * @return list of zone statistics
     */
    public List<ZoneStatsDto> getZoneStats() {
        log.debug("Computing zone statistics");

        List<String> zones = motherRepository.findAllZones();
        
        return zones.stream()
                .map(this::getZoneStats)
                .toList();
    }

    /**
     * Get statistics for a specific zone.
     *
     * @param zone the zone identifier
     * @return zone statistics
     */
    public ZoneStatsDto getZoneStats(String zone) {
        List<Volunteer> volunteersInZone = volunteerRepository.findAvailableByZone(zone);
        List<HelpRequest> requestsInZone = helpRequestRepository.findByZone(zone);

        long availableVolunteers = volunteersInZone.stream()
                .filter(v -> v.getAvailability() == AvailabilityStatus.AVAILABLE)
                .count();

        long activeRequests = requestsInZone.stream()
                .filter(r -> r.getStatus() == RequestStatus.ACCEPTED || 
                            r.getStatus() == RequestStatus.IN_PROGRESS)
                .count();

        long pendingEmergencies = requestsInZone.stream()
                .filter(r -> r.getRequestType() == RequestType.EMERGENCY && 
                            r.getStatus() == RequestStatus.PENDING)
                .count();

        return ZoneStatsDto.builder()
                .zone(zone)
                .motherCount(motherRepository.countByZone(zone))
                .volunteerCount(volunteersInZone.size())
                .availableVolunteers(availableVolunteers)
                .activeRequests(activeRequests)
                .pendingEmergencies(pendingEmergencies)
                .build();
    }

    /**
     * Get cases with optional filtering.
     *
     * @param zone   optional zone filter
     * @param status optional status filter
     * @param page   page number (0-based)
     * @param size   page size
     * @return list of case DTOs
     */
    public List<CaseDto> getCases(String zone, RequestStatus status, int page, int size) {
        log.debug("Fetching cases: zone={}, status={}, page={}, size={}", zone, status, page, size);

        List<HelpRequest> requests;

        if (zone != null && status != null) {
            requests = helpRequestRepository.findByZoneAndStatus(zone, status);
        } else if (zone != null) {
            requests = helpRequestRepository.findByZone(zone);
        } else if (status != null) {
            requests = helpRequestRepository.findByStatus(status);
        } else {
            requests = helpRequestRepository.findAll(
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
            ).getContent();
        }

        // Apply pagination if we didn't use repository pagination
        if (zone != null || status != null) {
            int start = page * size;
            int end = Math.min(start + size, requests.size());
            if (start >= requests.size()) {
                requests = List.of();
            } else {
                // Sort by createdAt descending
                requests = requests.stream()
                        .sorted(Comparator.comparing(HelpRequest::getCreatedAt).reversed())
                        .skip(start)
                        .limit(size)
                        .toList();
            }
        }

        return requests.stream()
                .map(CaseDto::fromEntity)
                .toList();
    }

    /**
     * Get volunteers with optional filtering.
     *
     * @param zone         optional zone filter
     * @param availability optional availability filter
     * @return list of volunteer DTOs
     */
    public List<VolunteerDto> getVolunteers(String zone, AvailabilityStatus availability) {
        log.debug("Fetching volunteers: zone={}, availability={}", zone, availability);

        List<Volunteer> volunteers;

        if (zone != null && availability != null) {
            if (availability == AvailabilityStatus.AVAILABLE) {
                volunteers = volunteerRepository.findAvailableByZone(zone);
            } else {
                // Filter manually for non-AVAILABLE status with zone
                volunteers = volunteerRepository.findAll().stream()
                        .filter(v -> v.getZones().contains(zone) && v.getAvailability() == availability)
                        .toList();
            }
        } else if (zone != null) {
            // All volunteers covering this zone (any availability)
            volunteers = volunteerRepository.findAll().stream()
                    .filter(v -> v.getZones().contains(zone))
                    .toList();
        } else if (availability != null) {
            volunteers = volunteerRepository.findByAvailability(availability);
        } else {
            volunteers = volunteerRepository.findAll();
        }

        return volunteers.stream()
                .map(VolunteerDto::fromEntity)
                .toList();
    }

    /**
     * Group mothers by zone.
     */
    private Map<String, Long> getMothersByZone(List<Mother> mothers) {
        return mothers.stream()
                .filter(m -> m.getZone() != null)
                .collect(Collectors.groupingBy(Mother::getZone, Collectors.counting()));
    }

    /**
     * Group requests by status.
     */
    private Map<String, Long> getRequestsByStatus(List<HelpRequest> requests) {
        return requests.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStatus().name(),
                        Collectors.counting()
                ));
    }

    /**
     * Group volunteers by skill type.
     */
    private Map<String, Long> getVolunteersBySkill(List<Volunteer> volunteers) {
        return volunteers.stream()
                .filter(v -> v.getSkillType() != null)
                .collect(Collectors.groupingBy(
                        v -> v.getSkillType().name(),
                        Collectors.counting()
                ));
    }

    /**
     * Get upcoming due dates clustered by date.
     * Returns due dates within the next 30 days.
     */
    private List<DashboardStatsDto.DueDateCluster> getUpcomingDueDates(List<Mother> mothers) {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);

        Map<LocalDate, Long> dueDateCounts = mothers.stream()
                .filter(m -> m.getDueDate() != null)
                .filter(m -> !m.getDueDate().isBefore(today) && !m.getDueDate().isAfter(thirtyDaysLater))
                .collect(Collectors.groupingBy(Mother::getDueDate, Collectors.counting()));

        return dueDateCounts.entrySet().stream()
                .map(e -> DashboardStatsDto.DueDateCluster.builder()
                        .date(e.getKey())
                        .count(e.getValue())
                        .build())
                .sorted(Comparator.comparing(DashboardStatsDto.DueDateCluster::date))
                .toList();
    }
}
