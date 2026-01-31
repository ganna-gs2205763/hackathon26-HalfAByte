package com.safebirth.domain.mother;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing mother registrations and operations.
 */
@Service
@Transactional(readOnly = true)
public class MotherService {

    private static final Logger log = LoggerFactory.getLogger(MotherService.class);

    private final MotherRepository motherRepository;

    public MotherService(MotherRepository motherRepository) {
        this.motherRepository = motherRepository;
    }

    /**
     * Register a new mother or update existing registration.
     *
     * @param phoneNumber the mother's phone number
     * @param camp        the camp identifier
     * @param zone        the zone within the camp
     * @param language    the preferred language
     * @return the registered mother
     */
    @Transactional
    public Mother register(String phoneNumber, String camp, String zone, Language language) {
        return register(phoneNumber, camp, zone, null, null, language);
    }

    /**
     * Register a new mother or update existing registration with full details.
     *
     * @param phoneNumber the mother's phone number
     * @param camp        the camp identifier
     * @param zone        the zone within the camp
     * @param dueDate     the expected delivery date (optional)
     * @param riskLevel   the risk level (optional, defaults to LOW)
     * @param language    the preferred language
     * @return the registered mother
     */
    @Transactional
    public Mother register(String phoneNumber, String camp, String zone,
                          LocalDate dueDate, RiskLevel riskLevel, Language language) {
        log.info("Registering mother: phone={}, camp={}, zone={}, dueDate={}, risk={}",
                maskPhone(phoneNumber), camp, zone, dueDate, riskLevel);

        Optional<Mother> existing = motherRepository.findByPhoneNumber(phoneNumber);

        if (existing.isPresent()) {
            Mother mother = existing.get();
            mother.setCamp(camp);
            mother.setZone(zone);
            if (dueDate != null) {
                mother.setDueDate(dueDate);
            }
            if (riskLevel != null) {
                mother.setRiskLevel(riskLevel);
            }
            mother.setPreferredLanguage(language);
            mother.setLastContactAt(LocalDateTime.now());
            log.info("Updated existing mother registration: {}", mother.getFormattedId());
            return motherRepository.save(mother);
        }

        Mother mother = Mother.builder()
                .phoneNumber(phoneNumber)
                .camp(camp)
                .zone(zone)
                .dueDate(dueDate)
                .preferredLanguage(language)
                .riskLevel(riskLevel != null ? riskLevel : RiskLevel.LOW)
                .registeredAt(LocalDateTime.now())
                .build();

        Mother saved = motherRepository.save(mother);
        log.info("New mother registered: {}", saved.getFormattedId());
        return saved;
    }

    /**
     * Find a mother by phone number.
     *
     * @param phoneNumber the phone number
     * @return the mother if found
     */
    public Optional<Mother> findByPhone(String phoneNumber) {
        return motherRepository.findByPhoneNumber(phoneNumber);
    }

    /**
     * Find a mother by ID.
     *
     * @param id the mother ID
     * @return the mother if found
     */
    public Optional<Mother> findById(Long id) {
        return motherRepository.findById(id);
    }

    /**
     * Get all mothers in a zone.
     *
     * @param zone the zone identifier
     * @return list of mothers
     */
    public List<Mother> findByZone(String zone) {
        return motherRepository.findByZone(zone);
    }

    /**
     * Update a mother's risk level.
     *
     * @param motherId  the mother's ID
     * @param riskLevel the new risk level
     * @return the updated mother
     */
    @Transactional
    public Mother updateRiskLevel(Long motherId, RiskLevel riskLevel) {
        Mother mother = motherRepository.findById(motherId)
                .orElseThrow(() -> new IllegalArgumentException("Mother not found: " + motherId));

        mother.setRiskLevel(riskLevel);
        log.info("Updated risk level for {}: {}", mother.getFormattedId(), riskLevel);
        return motherRepository.save(mother);
    }

    /**
     * Update a mother's due date.
     *
     * @param motherId the mother's ID
     * @param dueDate  the expected delivery date
     * @return the updated mother
     */
    @Transactional
    public Mother updateDueDate(Long motherId, LocalDate dueDate) {
        Mother mother = motherRepository.findById(motherId)
                .orElseThrow(() -> new IllegalArgumentException("Mother not found: " + motherId));

        mother.setDueDate(dueDate);
        log.info("Updated due date for {}: {}", mother.getFormattedId(), dueDate);
        return motherRepository.save(mother);
    }

    /**
     * Record contact with a mother.
     *
     * @param phoneNumber the mother's phone number
     */
    @Transactional
    public void recordContact(String phoneNumber) {
        motherRepository.findByPhoneNumber(phoneNumber)
                .ifPresent(mother -> {
                    mother.setLastContactAt(LocalDateTime.now());
                    motherRepository.save(mother);
                });
    }

    /**
     * Get count of mothers in a zone.
     *
     * @param zone the zone identifier
     * @return count of mothers
     */
    public long countByZone(String zone) {
        return motherRepository.countByZone(zone);
    }

    /**
     * Get all registered mothers.
     *
     * @return list of all mothers
     */
    public List<Mother> findAll() {
        return motherRepository.findAll();
    }

    /**
     * Get all distinct zones.
     *
     * @return list of zone identifiers
     */
    public List<String> getAllZones() {
        return motherRepository.findAllZones();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, phone.length() - 4) + "****";
    }
}
