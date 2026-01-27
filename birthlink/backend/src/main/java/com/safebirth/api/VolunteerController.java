package com.safebirth.api;

import com.safebirth.api.dto.AvailabilityUpdateRequest;
import com.safebirth.api.dto.CaseDto;
import com.safebirth.api.dto.VolunteerDto;
import com.safebirth.domain.helprequest.HelpRequestService;
import com.safebirth.domain.volunteer.VolunteerService;
import com.safebirth.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for volunteer app operations.
 * Uses X-Phone-Number header for volunteer identification (POC only - use JWT in production).
 */
@RestController
@RequestMapping("/api/volunteer")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Volunteer", description = "Volunteer mobile app API")
public class VolunteerController {

    private final VolunteerService volunteerService;
    private final HelpRequestService helpRequestService;

    /**
     * Get current volunteer's profile.
     * Note: In POC, we use phone number in header for identification.
     * In production, use proper JWT authentication.
     *
     * @param phoneNumber the volunteer's phone number (from header)
     * @return the volunteer profile
     */
    @GetMapping("/me")
    @Operation(summary = "Get my profile", 
               description = "Returns the authenticated volunteer's profile information")
    public ResponseEntity<VolunteerDto> getMyProfile(
            @Parameter(description = "Volunteer's phone number for authentication", required = true)
            @RequestHeader("X-Phone-Number") String phoneNumber
    ) {
        log.debug("GET /api/volunteer/me - phone={}", maskPhone(phoneNumber));
        
        return volunteerService.findByPhone(phoneNumber)
                .map(VolunteerDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer", phoneNumber));
    }

    /**
     * Get current volunteer's assigned cases.
     *
     * @param phoneNumber the volunteer's phone number (from header)
     * @return list of active cases assigned to the volunteer
     */
    @GetMapping("/me/cases")
    @Operation(summary = "Get my cases", 
               description = "Returns all active cases assigned to the authenticated volunteer")
    public ResponseEntity<List<CaseDto>> getMyCases(
            @Parameter(description = "Volunteer's phone number for authentication", required = true)
            @RequestHeader("X-Phone-Number") String phoneNumber
    ) {
        log.debug("GET /api/volunteer/me/cases - phone={}", maskPhone(phoneNumber));
        
        return volunteerService.findByPhone(phoneNumber)
                .map(volunteer -> {
                    List<CaseDto> cases = helpRequestService.findActiveByVolunteer(volunteer.getId())
                            .stream()
                            .map(CaseDto::fromEntity)
                            .toList();
                    return ResponseEntity.ok(cases);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer", phoneNumber));
    }

    /**
     * Update current volunteer's availability status.
     *
     * @param phoneNumber the volunteer's phone number (from header)
     * @param request     the availability update request
     * @return the updated volunteer profile
     */
    @PutMapping("/me/availability")
    @Operation(summary = "Update my availability", 
               description = "Updates the authenticated volunteer's availability status")
    public ResponseEntity<VolunteerDto> updateAvailability(
            @Parameter(description = "Volunteer's phone number for authentication", required = true)
            @RequestHeader("X-Phone-Number") String phoneNumber,
            @Valid @RequestBody AvailabilityUpdateRequest request
    ) {
        log.info("PUT /api/volunteer/me/availability - phone={}, status={}", 
                maskPhone(phoneNumber), request.availability());
        
        // Verify volunteer exists
        volunteerService.findByPhone(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer", phoneNumber));
        
        var updatedVolunteer = volunteerService.updateAvailability(phoneNumber, request.availability());
        return ResponseEntity.ok(VolunteerDto.fromEntity(updatedVolunteer));
    }

    /**
     * Mask phone number for logging purposes.
     *
     * @param phone the phone number
     * @return masked phone number
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, phone.length() - 4) + "****";
    }
}
