package com.safebirth.api;

import com.safebirth.api.dto.CaseDto;
import com.safebirth.api.dto.DashboardStatsDto;
import com.safebirth.api.dto.VolunteerDto;
import com.safebirth.api.dto.ZoneStatsDto;
import com.safebirth.domain.helprequest.HelpRequestService;
import com.safebirth.domain.helprequest.RequestStatus;
import com.safebirth.domain.volunteer.AvailabilityStatus;
import com.safebirth.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for NGO dashboard operations.
 * Provides endpoints for monitoring and managing maternal care coordination.
 */
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "NGO coordinator dashboard API")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;
    private final HelpRequestService helpRequestService;

    public DashboardController(DashboardService dashboardService, HelpRequestService helpRequestService) {
        this.dashboardService = dashboardService;
        this.helpRequestService = helpRequestService;
    }

    /**
     * Get comprehensive dashboard statistics.
     *
     * @return dashboard statistics including counts and distributions
     */
    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics",
               description = "Returns comprehensive statistics including totals, distributions by zone/status/skill, and upcoming due dates")
    public ResponseEntity<DashboardStatsDto> getStats() {
        log.debug("GET /api/dashboard/stats");
        return ResponseEntity.ok(dashboardService.getStats());
    }

    /**
     * Get all cases with optional filtering.
     *
     * @param zone   optional zone filter
     * @param status optional status filter
     * @param page   page number (0-based, default 0)
     * @param size   page size (default 20)
     * @return list of case DTOs
     */
    @GetMapping("/cases")
    @Operation(summary = "List cases",
               description = "Returns cases with optional filtering by zone and status, supports pagination")
    public ResponseEntity<List<CaseDto>> getCases(
            @Parameter(description = "Filter by zone (e.g., 'ZONE-A')")
            @RequestParam(required = false) String zone,
            @Parameter(description = "Filter by request status")
            @RequestParam(required = false) RequestStatus status,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        log.debug("GET /api/dashboard/cases - zone={}, status={}, page={}, size={}",
                zone, status, page, size);

        List<CaseDto> cases = dashboardService.getCases(zone, status, page, size);
        return ResponseEntity.ok(cases);
    }

    /**
     * Get a single case by ID.
     *
     * @param caseId the case identifier (e.g., HR-0001)
     * @return the case details
     */
    @GetMapping("/cases/{caseId}")
    @Operation(summary = "Get case details",
               description = "Returns detailed information about a specific case")
    public ResponseEntity<CaseDto> getCase(
            @Parameter(description = "Case ID (e.g., HR-0001)")
            @PathVariable String caseId
    ) {
        log.debug("GET /api/dashboard/cases/{}", caseId);

        return helpRequestService.findByCaseId(caseId)
                .map(CaseDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Case", caseId));
    }

    /**
     * Get all volunteers with optional filtering.
     *
     * @param zone         optional zone filter
     * @param availability optional availability status filter
     * @return list of volunteer DTOs
     */
    @GetMapping("/volunteers")
    @Operation(summary = "List volunteers",
               description = "Returns volunteers with optional filtering by zone and availability")
    public ResponseEntity<List<VolunteerDto>> getVolunteers(
            @Parameter(description = "Filter by zone coverage")
            @RequestParam(required = false) String zone,
            @Parameter(description = "Filter by availability status")
            @RequestParam(required = false) AvailabilityStatus availability
    ) {
        log.debug("GET /api/dashboard/volunteers - zone={}, availability={}", zone, availability);

        List<VolunteerDto> volunteers = dashboardService.getVolunteers(zone, availability);
        return ResponseEntity.ok(volunteers);
    }

    /**
     * Get zone-level statistics.
     *
     * @return list of statistics per zone
     */
    @GetMapping("/zones")
    @Operation(summary = "Get zone statistics",
               description = "Returns aggregated statistics for each zone")
    public ResponseEntity<List<ZoneStatsDto>> getZoneStats() {
        log.debug("GET /api/dashboard/zones");
        return ResponseEntity.ok(dashboardService.getZoneStats());
    }
}
