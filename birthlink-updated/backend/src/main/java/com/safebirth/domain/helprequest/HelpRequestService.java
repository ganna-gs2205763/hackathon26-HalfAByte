package com.safebirth.domain.helprequest;

import com.safebirth.domain.mother.Mother;
import com.safebirth.domain.volunteer.Volunteer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing help requests.
 */
@Service
@Transactional(readOnly = true)
public class HelpRequestService {

    private static final Logger log = LoggerFactory.getLogger(HelpRequestService.class);

    private final HelpRequestRepository helpRequestRepository;

    public HelpRequestService(HelpRequestRepository helpRequestRepository) {
        this.helpRequestRepository = helpRequestRepository;
    }

    /**
     * Create a new help request for a mother.
     *
     * @param mother      the mother requesting help
     * @param requestType the type of request
     * @return the created help request
     */
    @Transactional
    public HelpRequest createRequest(Mother mother, RequestType requestType) {
        log.info("Creating {} request for mother in zone {}", requestType, mother.getZone());

        String caseId = generateCaseId();

        HelpRequest request = HelpRequest.builder()
                .caseId(caseId)
                .mother(mother)
                .requestType(requestType)
                .status(RequestStatus.PENDING)
                .zone(mother.getZone())
                .riskLevel(mother.getRiskLevel())
                .dueDate(mother.getDueDate())
                .createdAt(LocalDateTime.now())
                .build();

        HelpRequest saved = helpRequestRepository.save(request);
        log.info("Created help request: {} for zone {}", caseId, mother.getZone());
        return saved;
    }

    /**
     * Find a help request by case ID.
     *
     * @param caseId the case ID
     * @return the request if found
     */
    public Optional<HelpRequest> findByCaseId(String caseId) {
        return helpRequestRepository.findByCaseId(normalizeId(caseId));
    }

    /**
     * Accept a help request.
     *
     * @param caseId    the case ID
     * @param volunteer the accepting volunteer
     * @return the updated request
     */
    @Transactional
    public HelpRequest acceptRequest(String caseId, Volunteer volunteer) {
        HelpRequest request = helpRequestRepository.findByCaseId(normalizeId(caseId))
                .orElseThrow(() -> new IllegalArgumentException("Help request not found: " + caseId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not pending: " + request.getStatus());
        }

        request.accept(volunteer);
        log.info("Request {} accepted by volunteer {}", caseId, volunteer.getFormattedId());
        return helpRequestRepository.save(request);
    }

    /**
     * Mark a request as in progress.
     *
     * @param caseId the case ID
     * @return the updated request
     */
    @Transactional
    public HelpRequest startProgress(String caseId) {
        HelpRequest request = helpRequestRepository.findByCaseId(normalizeId(caseId))
                .orElseThrow(() -> new IllegalArgumentException("Help request not found: " + caseId));

        if (request.getStatus() != RequestStatus.ACCEPTED) {
            throw new IllegalStateException("Request is not accepted: " + request.getStatus());
        }

        request.startProgress();
        log.info("Request {} marked as in progress", caseId);
        return helpRequestRepository.save(request);
    }

    /**
     * Complete a help request.
     *
     * @param caseId the case ID
     * @return the updated request
     */
    @Transactional
    public HelpRequest completeRequest(String caseId) {
        HelpRequest request = helpRequestRepository.findByCaseId(normalizeId(caseId))
                .orElseThrow(() -> new IllegalArgumentException("Help request not found: " + caseId));

        if (!request.isActive()) {
            throw new IllegalStateException("Request is not active: " + request.getStatus());
        }

        request.complete();
        log.info("Request {} completed", caseId);
        return helpRequestRepository.save(request);
    }

    /**
     * Cancel a help request.
     *
     * @param caseId the case ID
     * @return the updated request
     */
    @Transactional
    public HelpRequest cancelRequest(String caseId) {
        HelpRequest request = helpRequestRepository.findByCaseId(normalizeId(caseId))
                .orElseThrow(() -> new IllegalArgumentException("Help request not found: " + caseId));

        if (!request.isActive()) {
            throw new IllegalStateException("Request is not active: " + request.getStatus());
        }

        request.cancel();
        log.info("Request {} cancelled", caseId);
        return helpRequestRepository.save(request);
    }

    /**
     * Get pending requests in a zone.
     *
     * @param zone the zone identifier
     * @return list of pending requests
     */
    public List<HelpRequest> findPendingInZone(String zone) {
        return helpRequestRepository.findByZoneAndStatus(zone, RequestStatus.PENDING);
    }

    /**
     * Get active requests for a volunteer.
     *
     * @param volunteerId the volunteer's ID
     * @return list of active requests
     */
    public List<HelpRequest> findActiveByVolunteer(Long volunteerId) {
        return helpRequestRepository.findActiveByVolunteer(volunteerId);
    }

    /**
     * Get the most recent requests.
     *
     * @return list of recent requests
     */
    public List<HelpRequest> findRecent() {
        return helpRequestRepository.findTop20ByOrderByCreatedAtDesc();
    }

    /**
     * Count pending emergency requests.
     *
     * @return count of pending emergencies
     */
    public long countPendingEmergencies() {
        return helpRequestRepository.countPendingEmergencies();
    }

    /**
     * Increment the alerts sent counter.
     *
     * @param caseId the case ID
     */
    @Transactional
    public void incrementAlertsSent(String caseId) {
        helpRequestRepository.findByCaseId(normalizeId(caseId))
                .ifPresent(request -> {
                    request.setAlertsSent(request.getAlertsSent() + 1);
                    helpRequestRepository.save(request);
                });
    }

    /**
     * Get all help requests.
     *
     * @return list of all requests
     */
    public List<HelpRequest> findAll() {
        return helpRequestRepository.findAll();
    }

    /**
     * Generate a new unique case ID.
     *
     * @return the generated case ID
     */
    private String generateCaseId() {
        Integer maxNumber = helpRequestRepository.findMaxCaseNumber();
        int nextNumber = (maxNumber != null ? maxNumber : 0) + 1;
        return String.format("HR-%04d", nextNumber);
    }

    /**
     * Normalize a case ID to standard format.
     *
     * @param caseId the input case ID
     * @return normalized case ID
     */
    private String normalizeId(String caseId) {
        if (caseId == null) return null;
        String digits = caseId.replaceAll("[^0-9]", "");
        return "HR-" + digits;
    }
}
