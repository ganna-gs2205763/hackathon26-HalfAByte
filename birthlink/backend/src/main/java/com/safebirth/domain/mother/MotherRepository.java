package com.safebirth.domain.mother;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Mother entity operations.
 */
@Repository
public interface MotherRepository extends JpaRepository<Mother, Long> {

    /**
     * Find a mother by phone number.
     *
     * @param phoneNumber the phone number
     * @return the mother if found
     */
    Optional<Mother> findByPhoneNumber(String phoneNumber);

    /**
     * Check if a mother exists with the given phone number.
     *
     * @param phoneNumber the phone number
     * @return true if exists
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Find all mothers in a specific zone.
     *
     * @param zone the zone identifier
     * @return list of mothers in the zone
     */
    List<Mother> findByZone(String zone);

    /**
     * Find all mothers in a specific camp.
     *
     * @param camp the camp identifier
     * @return list of mothers in the camp
     */
    List<Mother> findByCamp(String camp);

    /**
     * Find all mothers with a specific risk level.
     *
     * @param riskLevel the risk level
     * @return list of mothers with that risk level
     */
    List<Mother> findByRiskLevel(RiskLevel riskLevel);

    /**
     * Count mothers by zone.
     *
     * @param zone the zone identifier
     * @return count of mothers
     */
    long countByZone(String zone);

    /**
     * Get all distinct zones.
     *
     * @return list of zone identifiers
     */
    @Query("SELECT DISTINCT m.zone FROM Mother m")
    List<String> findAllZones();

    /**
     * Get all distinct camps.
     *
     * @return list of camp identifiers
     */
    @Query("SELECT DISTINCT m.camp FROM Mother m")
    List<String> findAllCamps();

    /**
     * Find high-risk mothers in a zone.
     *
     * @param zone the zone identifier
     * @return list of high-risk mothers
     */
    @Query("SELECT m FROM Mother m WHERE m.zone = :zone AND m.riskLevel = 'HIGH'")
    List<Mother> findHighRiskInZone(@Param("zone") String zone);
}
