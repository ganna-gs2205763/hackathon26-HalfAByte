package com.safebirth.domain.volunteer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Volunteer entity operations.
 */
@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {

    /**
     * Find a volunteer by phone number.
     *
     * @param phoneNumber the phone number
     * @return the volunteer if found
     */
    Optional<Volunteer> findByPhoneNumber(String phoneNumber);

    /**
     * Check if a volunteer exists with the given phone number.
     *
     * @param phoneNumber the phone number
     * @return true if exists
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Find all volunteers in a specific camp.
     *
     * @param camp the camp identifier
     * @return list of volunteers
     */
    List<Volunteer> findByCamp(String camp);

    /**
     * Find all volunteers by status.
     *
     * @param status the status
     * @return list of volunteers
     */
    List<Volunteer> findByStatus(AvailabilityStatus status);

    /**
     * Alias for findByStatus for backward compatibility.
     * 
     * @deprecated Use findByStatus instead
     */
    default List<Volunteer> findByAvailability(AvailabilityStatus availability) {
        return findByStatus(availability);
    }

    /**
     * Find all available volunteers with a specific skill type.
     *
     * @param skillType the skill type
     * @param status    the status
     * @return list of matching volunteers
     */
    List<Volunteer> findBySkillTypeAndStatus(SkillType skillType, AvailabilityStatus status);

    /**
     * Alias for backward compatibility.
     * 
     * @deprecated Use findBySkillTypeAndStatus instead
     */
    default List<Volunteer> findBySkillTypeAndAvailability(SkillType skillType, AvailabilityStatus availability) {
        return findBySkillTypeAndStatus(skillType, availability);
    }

    /**
     * Find available volunteers who cover a specific zone.
     *
     * @param zone the zone identifier
     * @return list of volunteers covering the zone
     */
    @Query("SELECT v FROM Volunteer v JOIN v.zones z WHERE z = :zone AND v.status = 'AVAILABLE'")
    List<Volunteer> findAvailableByZone(@Param("zone") String zone);

    /**
     * Find available volunteers by zone and skill type, ordered by skill priority.
     *
     * @param zone the zone identifier
     * @return list of volunteers, prioritized by skill
     */
    @Query("SELECT v FROM Volunteer v JOIN v.zones z WHERE z = :zone AND v.status = 'AVAILABLE' ORDER BY v.skillType ASC")
    List<Volunteer> findAvailableByZoneOrderedBySkill(@Param("zone") String zone);

    /**
     * Count volunteers by status.
     *
     * @return count of volunteers
     */
    long countByStatus(AvailabilityStatus status);

    /**
     * Alias for backward compatibility.
     * 
     * @deprecated Use countByStatus instead
     */
    default long countByAvailability(AvailabilityStatus availability) {
        return countByStatus(availability);
    }

    /**
     * Get all distinct camps with volunteers.
     *
     * @return list of camp identifiers
     */
    @Query("SELECT DISTINCT v.camp FROM Volunteer v")
    List<String> findAllCamps();
}
