package com.safebirth.config;

import com.safebirth.domain.helprequest.HelpRequest;
import com.safebirth.domain.helprequest.HelpRequestRepository;
import com.safebirth.domain.helprequest.RequestStatus;
import com.safebirth.domain.helprequest.RequestType;
import com.safebirth.domain.mother.Language;
import com.safebirth.domain.mother.Mother;
import com.safebirth.domain.mother.MotherRepository;
import com.safebirth.domain.mother.RiskLevel;
import com.safebirth.domain.volunteer.AvailabilityStatus;
import com.safebirth.domain.volunteer.SkillType;
import com.safebirth.domain.volunteer.Volunteer;
import com.safebirth.domain.volunteer.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Sample data initializer for development environment.
 * Only runs when the "dev" profile is active.
 * 
 * Activate with: --spring.profiles.active=dev
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MotherRepository motherRepository;
    private final VolunteerRepository volunteerRepository;
    private final HelpRequestRepository helpRequestRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== Initializing sample data for development ===");

        // Only initialize if database is empty
        if (motherRepository.count() > 0 || volunteerRepository.count() > 0) {
            log.info("Database already contains data, skipping initialization");
            return;
        }

        createMothers();
        createVolunteers();
        createHelpRequests();

        log.info("=== Sample data initialization complete ===");
        log.info("Created {} mothers, {} volunteers, {} help requests",
                motherRepository.count(),
                volunteerRepository.count(),
                helpRequestRepository.count());
    }

    private void createMothers() {
        log.info("Creating sample mothers...");

        // Zone A mothers
        createMother("+962791111001", "Fatima", "CAMP-A", "ZONE-A", 
                LocalDate.now().plusDays(30), RiskLevel.HIGH, Language.ARABIC);
        createMother("+962791111002", "Aisha", "CAMP-A", "ZONE-A", 
                LocalDate.now().plusDays(45), RiskLevel.MEDIUM, Language.ARABIC);
        createMother("+962791111003", "Sara", "CAMP-A", "ZONE-A", 
                LocalDate.now().plusDays(60), RiskLevel.LOW, Language.ENGLISH);

        // Zone B mothers
        createMother("+962791111004", "Mariam", "CAMP-A", "ZONE-B", 
                LocalDate.now().plusDays(15), RiskLevel.HIGH, Language.ARABIC);
        createMother("+962791111005", "Nour", "CAMP-A", "ZONE-B", 
                LocalDate.now().plusDays(25), RiskLevel.LOW, Language.ARABIC);
        createMother("+962791111006", "Layla", "CAMP-A", "ZONE-B", 
                LocalDate.now().plusDays(90), RiskLevel.LOW, Language.ENGLISH);

        // Zone C mothers
        createMother("+962791111007", "Hana", "CAMP-A", "ZONE-C", 
                LocalDate.now().plusDays(7), RiskLevel.HIGH, Language.ARABIC);
        createMother("+962791111008", "Dina", "CAMP-A", "ZONE-C", 
                LocalDate.now().plusDays(50), RiskLevel.MEDIUM, Language.ARABIC);

        // Zone D mothers
        createMother("+962791111009", "Yasmin", "CAMP-A", "ZONE-D", 
                LocalDate.now().plusDays(20), RiskLevel.MEDIUM, Language.ARABIC);
        createMother("+962791111010", "Rania", "CAMP-A", "ZONE-D", 
                LocalDate.now().plusDays(35), RiskLevel.LOW, Language.ENGLISH);
    }

    private Mother createMother(String phone, String name, String camp, String zone,
                                LocalDate dueDate, RiskLevel risk, Language language) {
        Mother mother = Mother.builder()
                .phoneNumber(phone)
                .name(name)
                .camp(camp)
                .zone(zone)
                .dueDate(dueDate)
                .riskLevel(risk)
                .preferredLanguage(language)
                .registeredAt(LocalDateTime.now().minusDays((long) (Math.random() * 30)))
                .build();
        return motherRepository.save(mother);
    }

    private void createVolunteers() {
        log.info("Creating sample volunteers...");

        // Midwives (highest skill)
        createVolunteer("+962792222001", "Dr. Amina", "CAMP-A", SkillType.MIDWIFE,
                Set.of("ZONE-A", "ZONE-B"), AvailabilityStatus.AVAILABLE, Language.ARABIC);
        createVolunteer("+962792222002", "Dr. Nadia", "CAMP-A", SkillType.MIDWIFE,
                Set.of("ZONE-C", "ZONE-D"), AvailabilityStatus.BUSY, Language.ARABIC);

        // Nurses
        createVolunteer("+962792222003", "Samira", "CAMP-A", SkillType.NURSE,
                Set.of("ZONE-A", "ZONE-C"), AvailabilityStatus.AVAILABLE, Language.ARABIC);
        createVolunteer("+962792222004", "Reem", "CAMP-A", SkillType.NURSE,
                Set.of("ZONE-B", "ZONE-D"), AvailabilityStatus.AVAILABLE, Language.ENGLISH);

        // Trained birth attendants
        createVolunteer("+962792222005", "Huda", "CAMP-A", SkillType.TRAINED_ATTENDANT,
                Set.of("ZONE-A"), AvailabilityStatus.AVAILABLE, Language.ARABIC);
        createVolunteer("+962792222006", "Mona", "CAMP-A", SkillType.TRAINED_ATTENDANT,
                Set.of("ZONE-B"), AvailabilityStatus.OFFLINE, Language.ARABIC);
        createVolunteer("+962792222007", "Lina", "CAMP-A", SkillType.TRAINED_ATTENDANT,
                Set.of("ZONE-C", "ZONE-D"), AvailabilityStatus.AVAILABLE, Language.ENGLISH);

        // Community health workers
        createVolunteer("+962792222008", "Aya", "CAMP-A", SkillType.COMMUNITY_HEALTH_WORKER,
                Set.of("ZONE-A", "ZONE-B"), AvailabilityStatus.AVAILABLE, Language.ARABIC);
        createVolunteer("+962792222009", "Dana", "CAMP-A", SkillType.COMMUNITY_HEALTH_WORKER,
                Set.of("ZONE-C"), AvailabilityStatus.AVAILABLE, Language.ARABIC);
        createVolunteer("+962792222010", "Tala", "CAMP-A", SkillType.COMMUNITY_HEALTH_WORKER,
                Set.of("ZONE-D"), AvailabilityStatus.BUSY, Language.ENGLISH);
    }

    private Volunteer createVolunteer(String phone, String name, String camp, SkillType skill,
                                       Set<String> zones, AvailabilityStatus availability,
                                       Language language) {
        Volunteer volunteer = Volunteer.builder()
                .phoneNumber(phone)
                .name(name)
                .camp(camp)
                .skillType(skill)
                .zones(zones)
                .availability(availability)
                .preferredLanguage(language)
                .registeredAt(LocalDateTime.now().minusDays((long) (Math.random() * 60)))
                .completedCases((int) (Math.random() * 20))
                .build();
        return volunteerRepository.save(volunteer);
    }

    private void createHelpRequests() {
        log.info("Creating sample help requests...");

        var mothers = motherRepository.findAll();
        var volunteers = volunteerRepository.findAll();

        if (mothers.isEmpty() || volunteers.isEmpty()) {
            log.warn("Cannot create help requests - no mothers or volunteers");
            return;
        }

        // Pending emergency (high priority)
        Mother highRiskMother = mothers.stream()
                .filter(m -> m.getRiskLevel() == RiskLevel.HIGH)
                .findFirst().orElse(mothers.get(0));
        createHelpRequest("HR-0001", highRiskMother, RequestType.EMERGENCY, 
                RequestStatus.PENDING, null);

        // Pending support request
        createHelpRequest("HR-0002", mothers.get(1), RequestType.SUPPORT, 
                RequestStatus.PENDING, null);

        // Accepted emergency (in progress)
        Volunteer midwife = volunteers.stream()
                .filter(v -> v.getSkillType() == SkillType.MIDWIFE)
                .findFirst().orElse(volunteers.get(0));
        createHelpRequest("HR-0003", mothers.get(2), RequestType.EMERGENCY, 
                RequestStatus.ACCEPTED, midwife);

        // In-progress case
        Volunteer nurse = volunteers.stream()
                .filter(v -> v.getSkillType() == SkillType.NURSE)
                .findFirst().orElse(volunteers.get(1));
        HelpRequest inProgressRequest = createHelpRequest("HR-0004", mothers.get(3), 
                RequestType.SUPPORT, RequestStatus.IN_PROGRESS, nurse);
        
        // Completed case (today)
        HelpRequest completedRequest = createHelpRequest("HR-0005", mothers.get(4), 
                RequestType.SUPPORT, RequestStatus.COMPLETED, volunteers.get(2));
        completedRequest.setClosedAt(LocalDateTime.now().minusHours(2));
        helpRequestRepository.save(completedRequest);

        // Completed case (yesterday)
        HelpRequest yesterdayRequest = createHelpRequest("HR-0006", mothers.get(5), 
                RequestType.EMERGENCY, RequestStatus.COMPLETED, volunteers.get(0));
        yesterdayRequest.setClosedAt(LocalDateTime.now().minusDays(1));
        helpRequestRepository.save(yesterdayRequest);
    }

    private HelpRequest createHelpRequest(String caseId, Mother mother, RequestType type,
                                           RequestStatus status, Volunteer volunteer) {
        HelpRequest request = HelpRequest.builder()
                .caseId(caseId)
                .mother(mother)
                .requestType(type)
                .status(status)
                .zone(mother.getZone())
                .riskLevel(mother.getRiskLevel())
                .dueDate(mother.getDueDate())
                .createdAt(LocalDateTime.now().minusHours((long) (Math.random() * 48)))
                .build();

        if (volunteer != null) {
            request.setAcceptedBy(volunteer);
            request.setAcceptedAt(request.getCreatedAt().plusMinutes(5));
        }

        return helpRequestRepository.save(request);
    }
}
