package com.safebirth.domain.volunteer;

import com.safebirth.domain.mother.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service for managing volunteer registrations and operations.
 */
@Service
@Transactional(readOnly = true)
public class VolunteerService {

    private static final Logger log = LoggerFactory.getLogger(VolunteerService.class);

    private final VolunteerRepository volunteerRepository;

    public VolunteerService(VolunteerRepository volunteerRepository) {
        this.volunteerRepository = volunteerRepository;
    }

    /**
     * Register a new volunteer or update existing registration.
     *
     * @param phoneNumber the volunteer's phone number
     * @param name        the volunteer's name (optional)
     * @param camp        the camp identifier
     * @param skillType   the volunteer's skill type
     * @param zones       the zones the volunteer covers
     * @param language    the preferred language
     * @return the registered volunteer
     */
    @Transactional
    public Volunteer register(String phoneNumber, String name, String camp,
                              SkillType skillType, Set<String> zones, Language language) {
        log.info("Registering volunteer: phone={}, camp={}, skill={}",
                maskPhone(phoneNumber), camp, skillType);

        Optional<Volunteer> existing = volunteerRepository.findByPhoneNumber(phoneNumber);

        if (existing.isPresent()) {
            Volunteer volunteer = existing.get();
            volunteer.setName(name);
            volunteer.setCamp(camp);
            volunteer.setSkillType(skillType);
            volunteer.setZones(zones);
            volunteer.setPreferredLanguage(language);
            volunteer.setLastActiveAt(LocalDateTime.now());
            log.info("Updated existing volunteer: {}", volunteer.getFormattedId());
            return volunteerRepository.save(volunteer);
        }

        Volunteer volunteer = Volunteer.builder()
                .phoneNumber(phoneNumber)
                .name(name)
                .camp(camp)
                .skillType(skillType)
                .zones(zones)
                .preferredLanguage(language)
                .availability(AvailabilityStatus.AVAILABLE)
                .registeredAt(LocalDateTime.now())
                .build();

        Volunteer saved = volunteerRepository.save(volunteer);
        log.info("New volunteer registered: {}", saved.getFormattedId());
        return saved;
    }

    /**
     * Find a volunteer by phone number.
     *
     * @param phoneNumber the phone number
     * @return the volunteer if found
     */
    public Optional<Volunteer> findByPhone(String phoneNumber) {
        return volunteerRepository.findByPhoneNumber(phoneNumber);
    }

    /**
     * Find a volunteer by ID.
     *
     * @param id the volunteer ID
     * @return the volunteer if found
     */
    public Optional<Volunteer> findById(Long id) {
        return volunteerRepository.findById(id);
    }

    /**
     * Update a volunteer's availability status.
     *
     * @param phoneNumber the volunteer's phone number
     * @param status      the new availability status
     * @return the updated volunteer
     */
    @Transactional
    public Volunteer updateAvailability(String phoneNumber, AvailabilityStatus status) {
        Volunteer volunteer = volunteerRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("Volunteer not found: " + phoneNumber));

        volunteer.setAvailability(status);
        volunteer.setLastActiveAt(LocalDateTime.now());
        log.info("Updated availability for {}: {}", volunteer.getFormattedId(), status);
        return volunteerRepository.save(volunteer);
    }

    /**
     * Get available volunteers for a zone, ordered by skill priority.
     *
     * @param zone the zone identifier
     * @return list of available volunteers
     */
    public List<Volunteer> findAvailableForZone(String zone) {
        return volunteerRepository.findAvailableByZoneOrderedBySkill(zone);
    }

    /**
     * Get all available volunteers.
     *
     * @return list of available volunteers
     */
    public List<Volunteer> findAvailable() {
        return volunteerRepository.findByAvailability(AvailabilityStatus.AVAILABLE);
    }

    /**
     * Increment the completed cases count for a volunteer.
     *
     * @param volunteerId the volunteer's ID
     */
    @Transactional
    public void incrementCompletedCases(Long volunteerId) {
        volunteerRepository.findById(volunteerId)
                .ifPresent(volunteer -> {
                    volunteer.setCompletedCases(volunteer.getCompletedCases() + 1);
                    volunteerRepository.save(volunteer);
                    log.info("Incremented completed cases for {}: {}",
                            volunteer.getFormattedId(), volunteer.getCompletedCases());
                });
    }

    /**
     * Get all registered volunteers.
     *
     * @return list of all volunteers
     */
    public List<Volunteer> findAll() {
        return volunteerRepository.findAll();
    }

    /**
     * Count available volunteers.
     *
     * @return count of available volunteers
     */
    public long countAvailable() {
        return volunteerRepository.countByAvailability(AvailabilityStatus.AVAILABLE);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, phone.length() - 4) + "****";
    }
}
