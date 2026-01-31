package com.safebirth.domain.helprequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for volunteer responses to help requests.
 */
@Repository
public interface VolunteerResponseRepository extends JpaRepository<VolunteerResponse, Long> {

    /**
     * Find all responses for a case, ordered by ETA (shortest first).
     */
    List<VolunteerResponse> findByCaseIdOrderByEtaMinutesAsc(String caseId);

    /**
     * Find response by volunteer and case.
     */
    Optional<VolunteerResponse> findByCaseIdAndVolunteerId(String caseId, Long volunteerId);

    /**
     * Find the selected response for a case.
     */
    Optional<VolunteerResponse> findByCaseIdAndSelectedTrue(String caseId);
}
