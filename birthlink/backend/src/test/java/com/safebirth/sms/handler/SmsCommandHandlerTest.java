package com.safebirth.sms.handler;

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
import com.safebirth.sms.parser.CommandType;
import com.safebirth.sms.parser.SmsCommand;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for SmsCommandHandler.
 * Tests all command handlers with real database operations.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SmsCommandHandlerTest {

    @Autowired
    private SmsCommandHandler commandHandler;

    @Autowired
    private MotherRepository motherRepository;

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    @Autowired
    private MockSmsGateway mockSmsGateway;

    private static final String MOTHER_PHONE = "+1234567890";
    private static final String VOLUNTEER_PHONE = "+1234567891";

    @BeforeEach
    void setUp() {
        mockSmsGateway.clearOutbox();
    }

    @Nested
    @DisplayName("Mother Registration Tests")
    class MotherRegistrationTests {

        @Test
        @DisplayName("Should register new mother successfully")
        void testRegisterMother_Success() {
            SmsCommand command = createCommand(CommandType.REGISTER_MOTHER, MOTHER_PHONE, Language.ENGLISH,
                    Map.of("camp", "A", "zone", "3"));

            String response = commandHandler.handle(command);

            assertThat(response).contains("Registered");
            assertThat(response).contains("M-");
            assertThat(motherRepository.findByPhoneNumber(MOTHER_PHONE)).isPresent();
        }

        @Test
        @DisplayName("Should return error when camp is missing")
        void testRegisterMother_MissingCamp() {
            SmsCommand command = createCommand(CommandType.REGISTER_MOTHER, MOTHER_PHONE, Language.ENGLISH,
                    Map.of("zone", "3"));

            String response = commandHandler.handle(command);

            assertThat(response).contains("Camp is required");
            assertThat(motherRepository.findByPhoneNumber(MOTHER_PHONE)).isEmpty();
        }

        @Test
        @DisplayName("Should update existing mother registration")
        void testRegisterMother_UpdateExisting() {
            // First registration
            createMother(MOTHER_PHONE, "A", "3");

            // Second registration with different zone
            SmsCommand command = createCommand(CommandType.REGISTER_MOTHER, MOTHER_PHONE, Language.ENGLISH,
                    Map.of("camp", "A", "zone", "5"));

            String response = commandHandler.handle(command);

            assertThat(response).contains("Registered");
            Mother updated = motherRepository.findByPhoneNumber(MOTHER_PHONE).orElseThrow();
            assertThat(updated.getZone()).isEqualTo("5");
        }

        @Test
        @DisplayName("Should return Arabic response for Arabic registration")
        void testRegisterMother_ArabicResponse() {
            SmsCommand command = createCommand(CommandType.REGISTER_MOTHER, MOTHER_PHONE, Language.ARABIC,
                    Map.of("camp", "أ", "zone", "3"));

            String response = commandHandler.handle(command);

            assertThat(response).contains("تم التسجيل");
        }
    }

    @Nested
    @DisplayName("Volunteer Registration Tests")
    class VolunteerRegistrationTests {

        @Test
        @DisplayName("Should register new volunteer successfully")
        void testRegisterVolunteer_Success() {
            SmsCommand command = createCommand(CommandType.REGISTER_VOLUNTEER, VOLUNTEER_PHONE, Language.ENGLISH,
                    Map.of("name", "Fatima", "camp", "A", "zones", "3,4", "skillType", "MIDWIFE"));

            String response = commandHandler.handle(command);

            assertThat(response).contains("Volunteer registered");
            assertThat(response).contains("V-");
            Volunteer volunteer = volunteerRepository.findByPhoneNumber(VOLUNTEER_PHONE).orElseThrow();
            assertThat(volunteer.getSkillType()).isEqualTo(SkillType.MIDWIFE);
        }

        @Test
        @DisplayName("Should default to COMMUNITY_VOLUNTEER when skill not specified")
        void testRegisterVolunteer_DefaultSkill() {
            SmsCommand command = createCommand(CommandType.REGISTER_VOLUNTEER, VOLUNTEER_PHONE, Language.ENGLISH,
                    Map.of("name", "Ali", "camp", "A", "zones", "3"));

            String response = commandHandler.handle(command);

            Volunteer volunteer = volunteerRepository.findByPhoneNumber(VOLUNTEER_PHONE).orElseThrow();
            assertThat(volunteer.getSkillType()).isEqualTo(SkillType.COMMUNITY_VOLUNTEER);
        }
    }

    @Nested
    @DisplayName("Emergency Request Tests")
    class EmergencyRequestTests {

        @Test
        @DisplayName("Should create emergency request and notify volunteers")
        void testEmergency_CreatesRequestAndNotifies() {
            Mother mother = createMother(MOTHER_PHONE, "A", "3");
            Volunteer volunteer = createVolunteer(VOLUNTEER_PHONE, "Fatima", SkillType.MIDWIFE, "3");

            SmsCommand command = createCommand(CommandType.EMERGENCY, MOTHER_PHONE, Language.ENGLISH, Map.of());

            String response = commandHandler.handle(command);

            assertThat(response).contains("EMERGENCY received");
            assertThat(response).contains("1 volunteer");
            assertThat(mockSmsGateway.hasMessageTo(VOLUNTEER_PHONE)).isTrue();
        }

        @Test
        @DisplayName("Should reject emergency from unregistered mother")
        void testEmergency_UnregisteredMother() {
            SmsCommand command = createCommand(CommandType.EMERGENCY, MOTHER_PHONE, Language.ENGLISH, Map.of());

            String response = commandHandler.handle(command);

            assertThat(response).contains("not registered");
        }

        @Test
        @DisplayName("Should handle emergency when no volunteers available")
        void testEmergency_NoVolunteers() {
            createMother(MOTHER_PHONE, "A", "3");

            SmsCommand command = createCommand(CommandType.EMERGENCY, MOTHER_PHONE, Language.ENGLISH, Map.of());

            String response = commandHandler.handle(command);

            assertThat(response).contains("EMERGENCY received");
            assertThat(response).contains("No volunteers available");
        }
    }

    @Nested
    @DisplayName("Accept Case Tests")
    class AcceptCaseTests {

        @Test
        @DisplayName("Should accept case and notify mother")
        void testAcceptCase_Success() {
            Mother mother = createMother(MOTHER_PHONE, "A", "3");
            Volunteer volunteer = createVolunteer(VOLUNTEER_PHONE, "Fatima", SkillType.MIDWIFE, "3");
            HelpRequest request = createHelpRequest(mother, "HR-0001");

            SmsCommand command = createCommand(CommandType.ACCEPT_CASE, VOLUNTEER_PHONE, Language.ENGLISH,
                    Map.of("caseId", "HR-0001"));

            String response = commandHandler.handle(command);

            assertThat(response).contains("accepted case HR-0001");
            assertThat(mockSmsGateway.hasMessageTo(MOTHER_PHONE)).isTrue();
            
            HelpRequest updated = helpRequestRepository.findByCaseId("HR-0001").orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(RequestStatus.ACCEPTED);
        }

        @Test
        @DisplayName("Should reject if volunteer not registered")
        void testAcceptCase_UnregisteredVolunteer() {
            SmsCommand command = createCommand(CommandType.ACCEPT_CASE, VOLUNTEER_PHONE, Language.ENGLISH,
                    Map.of("caseId", "HR-0001"));

            String response = commandHandler.handle(command);

            assertThat(response).contains("not registered as a volunteer");
        }

        @Test
        @DisplayName("Should require case ID")
        void testAcceptCase_MissingCaseId() {
            createVolunteer(VOLUNTEER_PHONE, "Fatima", SkillType.MIDWIFE, "3");

            SmsCommand command = createCommand(CommandType.ACCEPT_CASE, VOLUNTEER_PHONE, Language.ENGLISH, Map.of());

            String response = commandHandler.handle(command);

            assertThat(response).contains("Case ID is required");
        }
    }

    @Nested
    @DisplayName("Complete Case Tests")
    class CompleteCaseTests {

        @Test
        @DisplayName("Should complete case and increment volunteer stats")
        void testCompleteCase_Success() {
            Mother mother = createMother(MOTHER_PHONE, "A", "3");
            Volunteer volunteer = createVolunteer(VOLUNTEER_PHONE, "Fatima", SkillType.MIDWIFE, "3");
            HelpRequest request = createHelpRequest(mother, "HR-0001");
            request.accept(volunteer);
            helpRequestRepository.save(request);

            SmsCommand command = createCommand(CommandType.COMPLETE_CASE, VOLUNTEER_PHONE, Language.ENGLISH,
                    Map.of("caseId", "HR-0001"));

            String response = commandHandler.handle(command);

            assertThat(response).contains("COMPLETE");
            assertThat(response).contains("Thank you");
            
            HelpRequest updated = helpRequestRepository.findByCaseId("HR-0001").orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(RequestStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should reject completion by non-assigned volunteer")
        void testCompleteCase_NotAssigned() {
            Mother mother = createMother(MOTHER_PHONE, "A", "3");
            Volunteer volunteer1 = createVolunteer(VOLUNTEER_PHONE, "Fatima", SkillType.MIDWIFE, "3");
            Volunteer volunteer2 = createVolunteer("+999", "Sara", SkillType.MIDWIFE, "3");
            HelpRequest request = createHelpRequest(mother, "HR-0001");
            request.accept(volunteer2); // Different volunteer accepted
            helpRequestRepository.save(request);

            SmsCommand command = createCommand(CommandType.COMPLETE_CASE, VOLUNTEER_PHONE, Language.ENGLISH,
                    Map.of("caseId", "HR-0001"));

            String response = commandHandler.handle(command);

            assertThat(response).contains("not assigned");
        }
    }

    @Nested
    @DisplayName("Availability Change Tests")
    class AvailabilityChangeTests {

        @Test
        @DisplayName("Should set volunteer as available")
        void testAvailable_Success() {
            Volunteer volunteer = createVolunteer(VOLUNTEER_PHONE, "Fatima", SkillType.MIDWIFE, "3");
            volunteer.setAvailability(AvailabilityStatus.BUSY);
            volunteerRepository.save(volunteer);

            SmsCommand command = createCommand(CommandType.AVAILABLE, VOLUNTEER_PHONE, Language.ENGLISH, Map.of());

            String response = commandHandler.handle(command);

            assertThat(response).contains("AVAILABLE");
            Volunteer updated = volunteerRepository.findByPhoneNumber(VOLUNTEER_PHONE).orElseThrow();
            assertThat(updated.getAvailability()).isEqualTo(AvailabilityStatus.AVAILABLE);
        }

        @Test
        @DisplayName("Should set volunteer as busy")
        void testBusy_Success() {
            createVolunteer(VOLUNTEER_PHONE, "Fatima", SkillType.MIDWIFE, "3");

            SmsCommand command = createCommand(CommandType.BUSY, VOLUNTEER_PHONE, Language.ENGLISH, Map.of());

            String response = commandHandler.handle(command);

            assertThat(response).contains("BUSY");
            Volunteer updated = volunteerRepository.findByPhoneNumber(VOLUNTEER_PHONE).orElseThrow();
            assertThat(updated.getAvailability()).isEqualTo(AvailabilityStatus.BUSY);
        }

        @Test
        @DisplayName("Should reject availability change from non-volunteer")
        void testAvailable_NotRegistered() {
            SmsCommand command = createCommand(CommandType.AVAILABLE, VOLUNTEER_PHONE, Language.ENGLISH, Map.of());

            String response = commandHandler.handle(command);

            assertThat(response).contains("not registered as a volunteer");
        }
    }

    @Nested
    @DisplayName("Status Query Tests")
    class StatusQueryTests {

        @Test
        @DisplayName("Should return mother status")
        void testStatus_Mother() {
            createMother(MOTHER_PHONE, "A", "3");

            SmsCommand command = createCommand(CommandType.STATUS, MOTHER_PHONE, Language.ENGLISH, Map.of());

            String response = commandHandler.handle(command);

            assertThat(response).contains("M-");
            assertThat(response).contains("Camp: A");
            assertThat(response).contains("Zone: 3");
        }

        @Test
        @DisplayName("Should return volunteer status")
        void testStatus_Volunteer() {
            createVolunteer(VOLUNTEER_PHONE, "Fatima", SkillType.MIDWIFE, "3");

            SmsCommand command = createCommand(CommandType.STATUS, VOLUNTEER_PHONE, Language.ENGLISH, Map.of());

            String response = commandHandler.handle(command);

            assertThat(response).contains("V-");
            assertThat(response).contains("AVAILABLE");
        }

        @Test
        @DisplayName("Should indicate not registered for unknown phone")
        void testStatus_NotRegistered() {
            SmsCommand command = createCommand(CommandType.STATUS, "+999", Language.ENGLISH, Map.of());

            String response = commandHandler.handle(command);

            assertThat(response).contains("not registered");
        }
    }

    @Nested
    @DisplayName("Help Command Tests")
    class HelpCommandTests {

        @Test
        @DisplayName("Should return English help message")
        void testHelp_English() {
            SmsCommand command = createCommand(CommandType.HELP, MOTHER_PHONE, Language.ENGLISH, Map.of());

            String response = commandHandler.handle(command);

            assertThat(response).contains("SafeBirth Commands");
            assertThat(response).contains("REG MOTHER");
            assertThat(response).contains("EMERGENCY");
        }

        @Test
        @DisplayName("Should return Arabic help message")
        void testHelp_Arabic() {
            SmsCommand command = createCommand(CommandType.HELP, MOTHER_PHONE, Language.ARABIC, Map.of());

            String response = commandHandler.handle(command);

            assertThat(response).contains("أوامر SafeBirth");
            assertThat(response).contains("تسجيل ام");
            assertThat(response).contains("طوارئ");
        }
    }

    @Nested
    @DisplayName("Unknown Command Tests")
    class UnknownCommandTests {

        @Test
        @DisplayName("Should return help suggestion for unknown commands")
        void testUnknown() {
            SmsCommand command = createCommand(CommandType.UNKNOWN, MOTHER_PHONE, Language.ENGLISH, Map.of());

            String response = commandHandler.handle(command);

            assertThat(response).contains("Unknown command");
            assertThat(response).contains("HELP");
        }
    }

    // Helper methods

    private SmsCommand createCommand(CommandType type, String phone, Language lang, Map<String, String> params) {
        return SmsCommand.builder()
                .type(type)
                .senderPhone(phone)
                .detectedLanguage(lang)
                .rawMessage("test")
                .parameters(new HashMap<>(params))
                .build();
    }

    private Mother createMother(String phone, String camp, String zone) {
        Mother mother = Mother.builder()
                .phoneNumber(phone)
                .camp(camp)
                .zone(zone)
                .riskLevel(RiskLevel.HIGH)
                .preferredLanguage(Language.ENGLISH)
                .dueDate(LocalDate.now().plusDays(7))
                .registeredAt(LocalDateTime.now())
                .build();
        return motherRepository.save(mother);
    }

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

    private HelpRequest createHelpRequest(Mother mother, String caseId) {
        HelpRequest request = HelpRequest.builder()
                .caseId(caseId)
                .mother(mother)
                .requestType(RequestType.EMERGENCY)
                .status(RequestStatus.PENDING)
                .zone(mother.getZone())
                .riskLevel(mother.getRiskLevel())
                .dueDate(mother.getDueDate())
                .createdAt(LocalDateTime.now())
                .build();
        return helpRequestRepository.save(request);
    }
}
