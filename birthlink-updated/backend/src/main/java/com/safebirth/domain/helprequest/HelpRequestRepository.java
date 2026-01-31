package com.safebirth.domain.helprequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for HelpRequest entity operations.
 */
@Repository
public interface HelpRequestRepository extends JpaRepository<HelpRequest, Long> {

    /**
     * Find a help request by its case ID.
     *
     * @param caseId the case ID (e.g., HR-0001)
     * @return the help request if found
     */
    Optional<HelpRequest> findByCaseId(String caseId);

    /**
     * Check if a case ID already exists.
     *
     * @param caseId the case ID
     * @return true if exists
     */
    boolean existsByCaseId(String caseId);

    /**
     * Find all requests with a specific status.
     *
     * @param status the request status
     * @return list of matching requests
     */
    List<HelpRequest> findByStatus(RequestStatus status);

    /**
     * Find all requests in a specific zone.
     *
     * @param zone the zone identifier
     * @return list of requests in the zone
     */
    List<HelpRequest> findByZone(String zone);

    /**
     * Find pending requests in a zone.
     *
     * @param zone the zone identifier
     * @return list of pending requests
     */
    List<HelpRequest> findByZoneAndStatus(String zone, RequestStatus status);

    /**
     * Find all requests for a specific mother.
     *
     * @param motherId the mother's ID
     * @return list of the mother's requests
     */
    List<HelpRequest> findByMotherId(Long motherId);

    /**
     * Find all requests accepted by a specific volunteer.
     *
     * @param volunteerId the volunteer's ID
     * @return list of the volunteer's requests
     */
    List<HelpRequest> findByAcceptedById(Long volunteerId);

    /**
     * Find active requests for a volunteer.
     *
     * @param volunteerId the volunteer's ID
     * @return list of active requests
     */
    @Query("SELECT hr FROM HelpRequest hr WHERE hr.acceptedBy.id = :volunteerId AND hr.status IN ('ACCEPTED', 'IN_PROGRESS')")
    List<HelpRequest> findActiveByVolunteer(@Param("volunteerId") Long volunteerId);

    /**
     * Count requests by status.
     *
     * @param status the status to count
     * @return count of requests
     */
    long countByStatus(RequestStatus status);

    /**
     * Count emergency requests that are pending.
     *
     * @return count of pending emergencies
     */
    @Query("SELECT COUNT(hr) FROM HelpRequest hr WHERE hr.requestType = 'EMERGENCY' AND hr.status = 'PENDING'")
    long countPendingEmergencies();

    /**
     * Find emergency requests created within a time window.
     *
     * @param since the start time
     * @return list of recent emergencies
     */
    @Query("SELECT hr FROM HelpRequest hr WHERE hr.requestType = 'EMERGENCY' AND hr.createdAt >= :since ORDER BY hr.createdAt DESC")
    List<HelpRequest> findRecentEmergencies(@Param("since") LocalDateTime since);

    /**
     * Get the most recent requests.
     *
     * @return list of recent requests
     */
    List<HelpRequest> findTop20ByOrderByCreatedAtDesc();

    /**
     * Find pending requests older than a threshold (for escalation).
     *
     * @param threshold the age threshold
     * @return list of old pending requests
     */
    @Query("SELECT hr FROM HelpRequest hr WHERE hr.status = 'PENDING' AND hr.createdAt < :threshold")
    List<HelpRequest> findPendingOlderThan(@Param("threshold") LocalDateTime threshold);

    /**
     * Get the maximum case number for generating new case IDs.
     *
     * @return the maximum case number or null
     */
    @Query("SELECT MAX(CAST(SUBSTRING(hr.caseId, 4) AS int)) FROM HelpRequest hr")
    Integer findMaxCaseNumber();
}
