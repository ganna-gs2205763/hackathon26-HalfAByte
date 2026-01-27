package com.safebirth.matching;

import com.safebirth.domain.helprequest.HelpRequest;
import com.safebirth.domain.helprequest.HelpRequestRepository;
import com.safebirth.domain.helprequest.RequestStatus;
import com.safebirth.domain.helprequest.RequestType;
import com.safebirth.domain.mother.Language;
import com.safebirth.domain.mother.Mother;
import com.safebirth.domain.mother.MotherRepository;
import com.safebirth.domain.mother.RiskLevel;
import com.safebirth.domain.volunteer.*;
import com.safebirth.sms.gateway.MockSmsGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for MatchingService.
 * Tests volunteer matching algorithm and notification flow.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MatchingServiceTest {

    @Autowired
    private MatchingService matchingService;

    @Autowired
    private MotherRepository motherRepository;

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    @Autowired
    private MockSmsGateway mockSmsGateway;

    private Mother mother;
    private HelpRequest emergencyRequest;

    @BeforeEach
    void setUp() {
        mockSmsGateway.clearOutbox();
        
        // Create a mother for testing
        mother = Mother.builder()
                .phoneNumber("+1234567890")
                .camp("A")
                .zone("3")
                .riskLevel(RiskLevel.HIGH)
                .dueDate(LocalDate.now().plusDays(7))
                .preferredLanguage(Language.ENGLISH)
                .registeredAt(LocalDateTime.now())
                .build();
        mother = motherRepository.save(mother);

        // Create an emergency help request
        emergencyRequest = HelpRequest.builder()
                .caseId("HR-0001")
                .mother(mother)
                .requestType(RequestType.EMERGENCY)
                .status(RequestStatus.PENDING)
                .zone("3")
                .riskLevel(RiskLevel.HIGH)
                .dueDate(mother.getDueDate())
                .createdAt(LocalDateTime.now())
                .build();
        emergencyRequest = helpRequestRepository.save(emergencyRequest);
    }

    @Nested
    @DisplayName("Priority Matching Tests")
    class PriorityMatchingTests {

        @Test
        @DisplayName("Should prioritize certified volunteers (MIDWIFE, NURSE)")
        void testMatch_PrioritizesCertified() {
            // Create volunteers with different skill levels
            createVolunteer("+111", "Midwife", SkillType.MIDWIFE, "3");
            createVolunteer("+222", "Trained", SkillType.TRAINED_ATTENDANT, "3");
            createVolunteer("+333", "Community", SkillType.COMMUNITY_VOLUNTEER, "3");

            List<Volunteer> matches = matchingService.findMatchingVolunteers(emergencyRequest, 5);

            assertThat(matches).hasSize(3);
            assertThat(matches.get(0).getSkillType()).isEqualTo(SkillType.MIDWIFE);
        }

        @Test
        @DisplayName("Should fall back to trained attendants when no certified available")
        void testMatch_FallsBackToTrained() {
            // Only create trained and community volunteers
            createVolunteer("+222", "Trained", SkillType.TRAINED_ATTENDANT, "3");
            createVolunteer("+333", "Community", SkillType.COMMUNITY_VOLUNTEER, "3");

            List<Volunteer> matches = matchingService.findMatchingVolunteers(emergencyRequest, 5);

            assertThat(matches).hasSize(2);
            assertThat(matches.get(0).getSkillType()).isEqualTo(SkillType.TRAINED_ATTENDANT);
        }

        @Test
        @DisplayName("Should return empty list when no volunteers in zone")
        void testMatch_NoMatchReturnsEmpty() {
            // Create volunteer in different zone
            createVolunteer("+111", "Midwife", SkillType.MIDWIFE, "5");

            List<Volunteer> matches = matchingService.findMatchingVolunteers(emergencyRequest, 5);

            assertThat(matches).isEmpty();
        }

        @Test
        @DisplayName("Should only match available volunteers")
        void testMatch_OnlyMatchesAvailable() {
            Volunteer available = createVolunteer("+111", "Available", SkillType.MIDWIFE, "3");
            Volunteer busy = createVolunteer("+222", "Busy", SkillType.MIDWIFE, "3");
            busy.setAvailability(AvailabilityStatus.BUSY);
            volunteerRepository.save(busy);

            List<Volunteer> matches = matchingService.findMatchingVolunteers(emergencyRequest, 5);

            assertThat(matches).hasSize(1);
            assertThat(matches.get(0).getPhoneNumber()).isEqualTo("+111");
        }
    }

    @Nested
    @DisplayName("Match and Notify Tests")
    class MatchAndNotifyTests {

        @Test
        @DisplayName("Should notify matched volunteers via SMS")
        void testNotify_SendsSmsToVolunteers() {
            createVolunteer("+111", "Fatima", SkillType.MIDWIFE, "3");
            createVolunteer("+222", "Sara", SkillType.NURSE, "3");

            List<Volunteer> notified = matchingService.matchAndNotify(emergencyRequest);

            assertThat(notified).hasSize(2);
            assertThat(mockSmsGateway.getOutboxSize()).isEqualTo(2);
            assertThat(mockSmsGateway.hasMessageTo("+111")).isTrue();
            assertThat(mockSmsGateway.hasMessageTo("+222")).isTrue();
        }

        @Test
        @DisplayName("Should send bilingual messages based on volunteer preference")
        void testNotify_SendsBilingualMessages() {
            // Create English-speaking volunteer
            Volunteer english = createVolunteer("+111", "John", SkillType.MIDWIFE, "3");
            english.setPreferredLanguage(Language.ENGLISH);
            volunteerRepository.save(english);

            // Create Arabic-speaking volunteer
            Volunteer arabic = createVolunteer("+222", "فاطمة", SkillType.NURSE, "3");
            arabic.setPreferredLanguage(Language.ARABIC);
            volunteerRepository.save(arabic);

            matchingService.matchAndNotify(emergencyRequest);

            // Check English message
            var englishMessages = mockSmsGateway.getMessagesSentTo("+111");
            assertThat(englishMessages).hasSize(1);
            assertThat(englishMessages.get(0).message()).contains("EMERGENCY");
            assertThat(englishMessages.get(0).message()).contains("ACCEPT");

            // Check Arabic message
            var arabicMessages = mockSmsGateway.getMessagesSentTo("+222");
            assertThat(arabicMessages).hasSize(1);
            assertThat(arabicMessages.get(0).message()).contains("طوارئ");
            assertThat(arabicMessages.get(0).message()).contains("قبول");
        }

        @Test
        @DisplayName("Should return empty list when no volunteers available")
        void testNotify_NoVolunteersReturnsEmpty() {
            List<Volunteer> notified = matchingService.matchAndNotify(emergencyRequest);

            assertThat(notified).isEmpty();
            assertThat(mockSmsGateway.getOutboxSize()).isZero();
        }

        @Test
        @DisplayName("Alert message should contain case ID")
        void testNotify_AlertContainsCaseId() {
            createVolunteer("+111", "Fatima", SkillType.MIDWIFE, "3");

            matchingService.matchAndNotify(emergencyRequest);

            var messages = mockSmsGateway.getOutbox();
            assertThat(messages.get(0).message()).contains("HR-0001");
        }

        @Test
        @DisplayName("Alert message should contain risk level")
        void testNotify_AlertContainsRiskLevel() {
            createVolunteer("+111", "Fatima", SkillType.MIDWIFE, "3");

            matchingService.matchAndNotify(emergencyRequest);

            var messages = mockSmsGateway.getOutbox();
            assertThat(messages.get(0).message()).contains("HIGH");
        }
    }

    @Nested
    @DisplayName("Alert Message Building Tests")
    class AlertMessageTests {

        @Test
        @DisplayName("Should build English alert message correctly")
        void testBuildAlertMessage_English() {
            Volunteer volunteer = createVolunteer("+111", "Fatima", SkillType.MIDWIFE, "3");

            String message = matchingService.buildAlertMessage(volunteer, emergencyRequest);

            assertThat(message).contains("EMERGENCY");
            assertThat(message).contains("Zone 3");
            assertThat(message).contains("Risk: HIGH");
            assertThat(message).contains("ACCEPT HR-0001");
        }

        @Test
        @DisplayName("Should build Arabic alert message correctly")
        void testBuildAlertMessage_Arabic() {
            Volunteer volunteer = createVolunteer("+111", "فاطمة", SkillType.MIDWIFE, "3");
            volunteer.setPreferredLanguage(Language.ARABIC);
            volunteerRepository.save(volunteer);

            String message = matchingService.buildAlertMessage(volunteer, emergencyRequest);

            assertThat(message).contains("طوارئ");
            assertThat(message).contains("منطقة 3");
            assertThat(message).contains("عالية");
            assertThat(message).contains("قبول HR-0001");
        }

        @Test
        @DisplayName("Should format due date as 'Tomorrow' when due tomorrow")
        void testBuildAlertMessage_DueTomorrow() {
            emergencyRequest.setDueDate(LocalDate.now().plusDays(1));
            helpRequestRepository.save(emergencyRequest);
            
            Volunteer volunteer = createVolunteer("+111", "Fatima", SkillType.MIDWIFE, "3");

            String message = matchingService.buildAlertMessage(volunteer, emergencyRequest);

            assertThat(message).contains("Tomorrow");
        }
    }

    @Nested
    @DisplayName("Zone Availability Tests")
    class ZoneAvailabilityTests {

        @Test
        @DisplayName("Should correctly check zone availability")
        void testHasAvailableVolunteers() {
            createVolunteer("+111", "Fatima", SkillType.MIDWIFE, "3");

            assertThat(matchingService.hasAvailableVolunteers("3")).isTrue();
            assertThat(matchingService.hasAvailableVolunteers("5")).isFalse();
        }

        @Test
        @DisplayName("Should correctly count available volunteers in zone")
        void testCountAvailableInZone() {
            createVolunteer("+111", "V1", SkillType.MIDWIFE, "3");
            createVolunteer("+222", "V2", SkillType.NURSE, "3");
            createVolunteer("+333", "V3", SkillType.MIDWIFE, "5");

            assertThat(matchingService.countAvailableInZone("3")).isEqualTo(2);
            assertThat(matchingService.countAvailableInZone("5")).isEqualTo(1);
            assertThat(matchingService.countAvailableInZone("7")).isZero();
        }
    }

    // Helper method
    private Volunteer createVolunteer(String phone, String name, SkillType skill, String zone) {
        Set<String> zones = new HashSet<>();
        zones.add(zone);
        
        Volunteer volunteer = Volunteer.builder()
                .phoneNumber(phone)
                .name(name)
                .camp("A")
                .skillType(skill)
                .zones(zones)
                .availability(AvailabilityStatus.AVAILABLE)
                .preferredLanguage(Language.ENGLISH)
                .registeredAt(LocalDateTime.now())
                .build();
        return volunteerRepository.save(volunteer);
    }
}
