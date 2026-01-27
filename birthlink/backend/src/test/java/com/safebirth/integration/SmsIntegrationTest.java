package com.safebirth.integration;

import com.safebirth.domain.helprequest.HelpRequest;
import com.safebirth.domain.helprequest.HelpRequestRepository;
import com.safebirth.domain.helprequest.RequestStatus;
import com.safebirth.domain.mother.Language;
import com.safebirth.domain.mother.Mother;
import com.safebirth.domain.mother.MotherRepository;
import com.safebirth.domain.mother.RiskLevel;
import com.safebirth.domain.volunteer.*;
import com.safebirth.sms.gateway.MockSmsGateway;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration tests for SMS flows.
 * Tests complete workflows from registration through case completion.
 * 
 * These tests verify:
 * - Mother registration in English and Arabic
 * - Volunteer registration in English and Arabic
 * - Emergency request flow with volunteer matching
 * - Case acceptance and completion
 * - Status queries and availability changes
 * - Error handling for unregistered users
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SmsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MotherRepository motherRepository;

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    @Autowired
    private MockSmsGateway mockSmsGateway;

    private static final String MOTHER_PHONE_EN = "+201234567890";
    private static final String MOTHER_PHONE_AR = "+201234567891";
    private static final String VOLUNTEER_PHONE_EN = "+201234567892";
    private static final String VOLUNTEER_PHONE_AR = "+201234567893";
    private static final String UNREGISTERED_PHONE = "+201234567899";

    @BeforeEach
    void setUp() {
        mockSmsGateway.clearOutbox();
    }

    // ==================== Mother Registration Tests ====================

    @Nested
    @DisplayName("Mother Registration Flow")
    class MotherRegistrationTests {

        @Test
        @DisplayName("Should register mother with English command")
        void testMotherRegistration_English() throws Exception {
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", MOTHER_PHONE_EN)
                            .param("To", "+1555000000")
                            .param("Body", "REG MOTHER CAMP A ZONE 3 DUE 15-02 RISK HIGH"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                    .andExpect(content().string(containsString("Registered")))
                    .andExpect(content().string(containsString("M-")));

            // Verify mother was created in database
            var motherOpt = motherRepository.findByPhoneNumber(MOTHER_PHONE_EN);
            assertThat(motherOpt).isPresent();
            
            Mother mother = motherOpt.get();
            assertThat(mother.getCamp()).isEqualTo("A");
            assertThat(mother.getZone()).isEqualTo("3");
            assertThat(mother.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
            assertThat(mother.getPreferredLanguage()).isEqualTo(Language.ENGLISH);
        }

        @Test
        @DisplayName("Should register mother with Arabic command")
        void testMotherRegistration_Arabic() throws Exception {
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", MOTHER_PHONE_AR)
                            .param("To", "+1555000000")
                            .param("Body", "تسجيل ام مخيم أ منطقة 3 موعد 15-02 خطورة عالية"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                    .andExpect(content().string(containsString("تم التسجيل")));

            // Verify mother was created with Arabic language preference
            var motherOpt = motherRepository.findByPhoneNumber(MOTHER_PHONE_AR);
            assertThat(motherOpt).isPresent();
            
            Mother mother = motherOpt.get();
            assertThat(mother.getPreferredLanguage()).isEqualTo(Language.ARABIC);
        }

        @Test
        @DisplayName("Should return error when camp is missing")
        void testMotherRegistration_MissingCamp() throws Exception {
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+201111111111")
                            .param("To", "+1555000000")
                            .param("Body", "REG MOTHER ZONE 3"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Camp is required")));
        }

        @Test
        @DisplayName("Should return error when zone is missing")
        void testMotherRegistration_MissingZone() throws Exception {
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+201111111112")
                            .param("To", "+1555000000")
                            .param("Body", "REG MOTHER CAMP A"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Zone is required")));
        }
    }

    // ==================== Volunteer Registration Tests ====================

    @Nested
    @DisplayName("Volunteer Registration Flow")
    class VolunteerRegistrationTests {

        @Test
        @DisplayName("Should register volunteer with English command")
        void testVolunteerRegistration_English() throws Exception {
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", VOLUNTEER_PHONE_EN)
                            .param("To", "+1555000000")
                            .param("Body", "REG VOLUNTEER NAME Sarah CAMP A ZONE 3 SKILL MIDWIFE"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Volunteer registered")))
                    .andExpect(content().string(containsString("V-")));

            // Verify volunteer was created
            var volunteerOpt = volunteerRepository.findByPhoneNumber(VOLUNTEER_PHONE_EN);
            assertThat(volunteerOpt).isPresent();
            
            Volunteer volunteer = volunteerOpt.get();
            assertThat(volunteer.getName()).isEqualTo("Sarah");
            assertThat(volunteer.getSkillType()).isEqualTo(SkillType.MIDWIFE);
            assertThat(volunteer.getZones()).contains("3");
            assertThat(volunteer.getAvailability()).isEqualTo(AvailabilityStatus.AVAILABLE);
        }

        @Test
        @DisplayName("Should register volunteer with Arabic command")
        void testVolunteerRegistration_Arabic() throws Exception {
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", VOLUNTEER_PHONE_AR)
                            .param("To", "+1555000000")
                            .param("Body", "تسجيل متطوع الاسم فاطمة مخيم أ منطقة 3,4 مهارة قابلة"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("تم تسجيل المتطوع")));

            // Verify volunteer was created with Arabic language preference
            var volunteerOpt = volunteerRepository.findByPhoneNumber(VOLUNTEER_PHONE_AR);
            assertThat(volunteerOpt).isPresent();
            
            Volunteer volunteer = volunteerOpt.get();
            assertThat(volunteer.getPreferredLanguage()).isEqualTo(Language.ARABIC);
        }

        @Test
        @DisplayName("Should register volunteer with multiple zones")
        void testVolunteerRegistration_MultipleZones() throws Exception {
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+201234500001")
                            .param("To", "+1555000000")
                            .param("Body", "REG VOLUNTEER NAME TestVol CAMP B ZONE 1,2,3 SKILL NURSE"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Volunteer registered")));

            var volunteerOpt = volunteerRepository.findByPhoneNumber("+201234500001");
            assertThat(volunteerOpt).isPresent();
            assertThat(volunteerOpt.get().getZones()).containsExactlyInAnyOrder("1", "2", "3");
        }
    }

    // ==================== Emergency Flow Tests ====================

    @Nested
    @DisplayName("Emergency Request Flow")
    class EmergencyFlowTests {

        @Test
        @DisplayName("Should create emergency and notify volunteers")
        void testEmergency_TriggersMatching() throws Exception {
            // Setup: Create a registered mother
            Mother mother = createMother("+202000000001", "A", "5");
            
            // Setup: Create an available volunteer in the same zone
            Volunteer volunteer = createVolunteer("+202000000002", "Nurse Mary", "A", 
                    SkillType.NURSE, Set.of("5"));

            // Send emergency request
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+202000000001")
                            .param("To", "+1555000000")
                            .param("Body", "EMERGENCY"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("EMERGENCY received")))
                    .andExpect(content().string(containsString("volunteer")));

            // Verify help request was created
            var requests = helpRequestRepository.findAll();
            assertThat(requests).hasSizeGreaterThanOrEqualTo(1);
            
            // Verify volunteer was notified
            assertThat(mockSmsGateway.hasMessageTo("+202000000002")).isTrue();
            assertThat(mockSmsGateway.hasMessageContaining("EMERGENCY")).isTrue();
        }

        @Test
        @DisplayName("Should handle emergency with Arabic command")
        void testEmergency_Arabic() throws Exception {
            Mother mother = createMother("+202000000003", "B", "2");
            mother.setPreferredLanguage(Language.ARABIC);
            motherRepository.save(mother);

            Volunteer volunteer = createVolunteer("+202000000004", "فاطمة", "B", 
                    SkillType.MIDWIFE, Set.of("2"));

            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+202000000003")
                            .param("To", "+1555000000")
                            .param("Body", "طوارئ"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("تم استلام الطوارئ")));
        }

        @Test
        @DisplayName("Should return error for unregistered mother")
        void testEmergency_UnregisteredMother() throws Exception {
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", UNREGISTERED_PHONE)
                            .param("To", "+1555000000")
                            .param("Body", "EMERGENCY"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("not registered")));
        }

        @Test
        @DisplayName("Should notify when no volunteers available")
        void testEmergency_NoVolunteersAvailable() throws Exception {
            // Create mother in a zone with no volunteers
            Mother mother = createMother("+202000000005", "C", "99");

            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+202000000005")
                            .param("To", "+1555000000")
                            .param("Body", "EMERGENCY"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("No volunteers available")));
        }
    }

    // ==================== Accept Case Flow Tests ====================

    @Nested
    @DisplayName("Accept Case Flow")
    class AcceptCaseFlowTests {

        @Test
        @DisplayName("Should accept case and notify mother")
        void testAcceptCase_Success() throws Exception {
            // Setup: Create mother, volunteer, and help request
            Mother mother = createMother("+203000000001", "A", "7");
            Volunteer volunteer = createVolunteer("+203000000002", "Vol1", "A", 
                    SkillType.TRAINED_ATTENDANT, Set.of("7"));
            HelpRequest request = createHelpRequest(mother, "HR-TEST-001");

            // Accept the case
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+203000000002")
                            .param("To", "+1555000000")
                            .param("Body", "ACCEPT HR-TEST-001"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("accepted")))
                    .andExpect(content().string(containsString("HR-TEST-001")));

            // Verify request status updated
            var updatedRequest = helpRequestRepository.findByCaseId("HR-TEST-001");
            assertThat(updatedRequest).isPresent();
            assertThat(updatedRequest.get().getStatus()).isEqualTo(RequestStatus.ACCEPTED);
            assertThat(updatedRequest.get().getAcceptedBy()).isNotNull();

            // Verify mother was notified
            assertThat(mockSmsGateway.hasMessageTo("+203000000001")).isTrue();
        }

        @Test
        @DisplayName("Should accept case with Arabic command")
        void testAcceptCase_Arabic() throws Exception {
            Mother mother = createMother("+203000000003", "B", "8");
            Volunteer volunteer = createVolunteer("+203000000004", "أحمد", "B", 
                    SkillType.MIDWIFE, Set.of("8"));
            volunteer.setPreferredLanguage(Language.ARABIC);
            volunteerRepository.save(volunteer);
            
            HelpRequest request = createHelpRequest(mother, "HR-TEST-002");

            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+203000000004")
                            .param("To", "+1555000000")
                            .param("Body", "قبول HR-TEST-002"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("قبلت")));
        }

        @Test
        @DisplayName("Should return error for invalid case ID")
        void testAcceptCase_InvalidCaseId() throws Exception {
            Volunteer volunteer = createVolunteer("+203000000005", "Vol2", "A", 
                    SkillType.NURSE, Set.of("1"));

            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+203000000005")
                            .param("To", "+1555000000")
                            .param("Body", "ACCEPT HR-INVALID-999"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("not found")));
        }

        @Test
        @DisplayName("Should return error for non-volunteer")
        void testAcceptCase_NotVolunteer() throws Exception {
            Mother mother = createMother("+203000000006", "A", "9");
            HelpRequest request = createHelpRequest(mother, "HR-TEST-003");

            // Mother trying to accept (should fail)
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+203000000006")
                            .param("To", "+1555000000")
                            .param("Body", "ACCEPT HR-TEST-003"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("not registered as a volunteer")));
        }
    }

    // ==================== Complete Case Flow Tests ====================

    @Nested
    @DisplayName("Complete Case Flow")
    class CompleteCaseFlowTests {

        @Test
        @DisplayName("Should complete case successfully")
        void testCompleteCase_Success() throws Exception {
            // Setup
            Mother mother = createMother("+204000000001", "A", "1");
            Volunteer volunteer = createVolunteer("+204000000002", "CompleteVol", "A", 
                    SkillType.MIDWIFE, Set.of("1"));
            HelpRequest request = createHelpRequest(mother, "HR-COMPLETE-001");
            
            // Set request as accepted by this volunteer
            request.setStatus(RequestStatus.ACCEPTED);
            request.setAcceptedBy(volunteer);
            request.setAcceptedAt(LocalDateTime.now());
            helpRequestRepository.save(request);

            // Complete the case
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+204000000002")
                            .param("To", "+1555000000")
                            .param("Body", "COMPLETE HR-COMPLETE-001"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("COMPLETE")))
                    .andExpect(content().string(containsString("HR-COMPLETE-001")));

            // Verify request status updated
            var updatedRequest = helpRequestRepository.findByCaseId("HR-COMPLETE-001");
            assertThat(updatedRequest).isPresent();
            assertThat(updatedRequest.get().getStatus()).isEqualTo(RequestStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should complete case with Arabic command")
        void testCompleteCase_Arabic() throws Exception {
            Mother mother = createMother("+204000000003", "B", "2");
            Volunteer volunteer = createVolunteer("+204000000004", "سارة", "B", 
                    SkillType.NURSE, Set.of("2"));
            volunteer.setPreferredLanguage(Language.ARABIC);
            volunteerRepository.save(volunteer);
            
            HelpRequest request = createHelpRequest(mother, "HR-COMPLETE-002");
            request.setStatus(RequestStatus.ACCEPTED);
            request.setAcceptedBy(volunteer);
            helpRequestRepository.save(request);

            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+204000000004")
                            .param("To", "+1555000000")
                            .param("Body", "انهاء HR-COMPLETE-002"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("اكتمال")));
        }

        @Test
        @DisplayName("Should fail if volunteer is not assigned to case")
        void testCompleteCase_NotAssigned() throws Exception {
            Mother mother = createMother("+204000000005", "A", "3");
            Volunteer volunteer1 = createVolunteer("+204000000006", "Vol1", "A", 
                    SkillType.MIDWIFE, Set.of("3"));
            Volunteer volunteer2 = createVolunteer("+204000000007", "Vol2", "A", 
                    SkillType.NURSE, Set.of("3"));
            
            HelpRequest request = createHelpRequest(mother, "HR-COMPLETE-003");
            request.setStatus(RequestStatus.ACCEPTED);
            request.setAcceptedBy(volunteer1); // Assigned to volunteer1
            helpRequestRepository.save(request);

            // volunteer2 tries to complete (should fail)
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+204000000007")
                            .param("To", "+1555000000")
                            .param("Body", "COMPLETE HR-COMPLETE-003"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("not assigned")));
        }
    }

    // ==================== Availability Status Tests ====================

    @Nested
    @DisplayName("Availability Status Flow")
    class AvailabilityStatusTests {

        @Test
        @DisplayName("Should set volunteer as available")
        void testSetAvailable() throws Exception {
            Volunteer volunteer = createVolunteer("+205000000001", "AvailVol", "A", 
                    SkillType.COMMUNITY_VOLUNTEER, Set.of("1"));
            volunteer.setAvailability(AvailabilityStatus.BUSY);
            volunteerRepository.save(volunteer);

            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+205000000001")
                            .param("To", "+1555000000")
                            .param("Body", "AVAILABLE"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("AVAILABLE")));

            var updated = volunteerRepository.findByPhoneNumber("+205000000001");
            assertThat(updated).isPresent();
            assertThat(updated.get().getAvailability()).isEqualTo(AvailabilityStatus.AVAILABLE);
        }

        @Test
        @DisplayName("Should set volunteer as busy")
        void testSetBusy() throws Exception {
            Volunteer volunteer = createVolunteer("+205000000002", "BusyVol", "A", 
                    SkillType.COMMUNITY_VOLUNTEER, Set.of("2"));

            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+205000000002")
                            .param("To", "+1555000000")
                            .param("Body", "BUSY"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("BUSY")));

            var updated = volunteerRepository.findByPhoneNumber("+205000000002");
            assertThat(updated).isPresent();
            assertThat(updated.get().getAvailability()).isEqualTo(AvailabilityStatus.BUSY);
        }

        @Test
        @DisplayName("Should set availability with Arabic command")
        void testSetAvailable_Arabic() throws Exception {
            Volunteer volunteer = createVolunteer("+205000000003", "عمر", "B", 
                    SkillType.NURSE, Set.of("3"));
            volunteer.setPreferredLanguage(Language.ARABIC);
            volunteerRepository.save(volunteer);

            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+205000000003")
                            .param("To", "+1555000000")
                            .param("Body", "متاح"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("متاح")));
        }
    }

    // ==================== Status Query Tests ====================

    @Nested
    @DisplayName("Status Query Flow")
    class StatusQueryTests {

        @Test
        @DisplayName("Should return status for registered mother")
        void testStatus_Mother() throws Exception {
            Mother mother = createMother("+206000000001", "C", "4");

            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+206000000001")
                            .param("To", "+1555000000")
                            .param("Body", "STATUS"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("M-")))
                    .andExpect(content().string(containsString("Camp: C")))
                    .andExpect(content().string(containsString("Zone: 4")));
        }

        @Test
        @DisplayName("Should return status for registered volunteer")
        void testStatus_Volunteer() throws Exception {
            Volunteer volunteer = createVolunteer("+206000000002", "StatusVol", "D", 
                    SkillType.TRAINED_ATTENDANT, Set.of("5"));
            volunteer.setCompletedCases(10);
            volunteerRepository.save(volunteer);

            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+206000000002")
                            .param("To", "+1555000000")
                            .param("Body", "STATUS"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("V-")))
                    .andExpect(content().string(containsString("AVAILABLE")))
                    .andExpect(content().string(containsString("10")));
        }

        @Test
        @DisplayName("Should return registration prompt for unregistered user")
        void testStatus_Unregistered() throws Exception {
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+206000000099")
                            .param("To", "+1555000000")
                            .param("Body", "STATUS"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("not registered")));
        }
    }

    // ==================== Help Command Tests ====================

    @Nested
    @DisplayName("Help Command Flow")
    class HelpCommandTests {

        @Test
        @DisplayName("Should return help message in English")
        void testHelp_English() throws Exception {
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+207000000001")
                            .param("To", "+1555000000")
                            .param("Body", "HELP"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("SafeBirth Commands")))
                    .andExpect(content().string(containsString("EMERGENCY")))
                    .andExpect(content().string(containsString("ACCEPT")));
        }

        @Test
        @DisplayName("Should return help message in Arabic")
        void testHelp_Arabic() throws Exception {
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+207000000002")
                            .param("To", "+1555000000")
                            .param("Body", "مساعدة"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("أوامر SafeBirth")))
                    .andExpect(content().string(containsString("طوارئ")));
        }

        @Test
        @DisplayName("Should return help for unknown command")
        void testUnknownCommand_ReturnsHelp() throws Exception {
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+207000000003")
                            .param("To", "+1555000000")
                            .param("Body", "RANDOM GIBBERISH XYZ123"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Unknown command")));
        }
    }

    // ==================== Helper Methods ====================

    private Mother createMother(String phone, String camp, String zone) {
        Mother mother = Mother.builder()
                .phoneNumber(phone)
                .camp(camp)
                .zone(zone)
                .riskLevel(RiskLevel.MEDIUM)
                .preferredLanguage(Language.ENGLISH)
                .dueDate(LocalDate.now().plusDays(30))
                .registeredAt(LocalDateTime.now())
                .build();
        return motherRepository.save(mother);
    }

    private Volunteer createVolunteer(String phone, String name, String camp, 
                                       SkillType skill, Set<String> zones) {
        Volunteer volunteer = Volunteer.builder()
                .phoneNumber(phone)
                .name(name)
                .camp(camp)
                .skillType(skill)
                .zones(new HashSet<>(zones))
                .availability(AvailabilityStatus.AVAILABLE)
                .preferredLanguage(Language.ENGLISH)
                .registeredAt(LocalDateTime.now())
                .completedCases(0)
                .build();
        return volunteerRepository.save(volunteer);
    }

    private HelpRequest createHelpRequest(Mother mother, String caseId) {
        HelpRequest request = HelpRequest.builder()
                .caseId(caseId)
                .mother(mother)
                .requestType(com.safebirth.domain.helprequest.RequestType.EMERGENCY)
                .status(RequestStatus.PENDING)
                .zone(mother.getZone())
                .riskLevel(mother.getRiskLevel())
                .createdAt(LocalDateTime.now())
                .build();
        return helpRequestRepository.save(request);
    }
}
