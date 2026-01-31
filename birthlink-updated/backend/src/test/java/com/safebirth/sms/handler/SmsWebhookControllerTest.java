package com.safebirth.sms.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SmsWebhookController.
 * Tests the full flow from HTTP request to database operations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SmsWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MotherRepository motherRepository;

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private MockSmsGateway mockSmsGateway;

    private static final String MOTHER_PHONE = "+1234567890";
    private static final String VOLUNTEER_PHONE = "+1234567891";

    @BeforeEach
    void setUp() {
        mockSmsGateway.clearOutbox();
    }

    @Nested
    @DisplayName("Twilio Webhook Tests")
    class TwilioWebhookTests {

        @Test
        @DisplayName("Should process mother registration via webhook")
        void testIncomingSms_MotherRegistration() throws Exception {
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", MOTHER_PHONE)
                            .param("To", "+1555000000")
                            .param("Body", "REG MOTHER CAMP A ZONE 3"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                    .andExpect(content().string(containsString("Registered")))
                    .andExpect(content().string(containsString("M-")));

            // Verify mother was created in database
            assertThat(motherRepository.findByPhoneNumber(MOTHER_PHONE)).isPresent();
        }

        @Test
        @DisplayName("Should process Arabic mother registration")
        void testIncomingSms_ArabicMotherRegistration() throws Exception {
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", MOTHER_PHONE)
                            .param("To", "+1555000000")
                            .param("Body", "تسجيل ام مخيم أ منطقة 3"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("تم التسجيل")));

            assertThat(motherRepository.findByPhoneNumber(MOTHER_PHONE)).isPresent();
        }

        @Test
        @DisplayName("Should trigger matching on emergency request")
        void testIncomingSms_EmergencyTriggersMatching() throws Exception {
            // Setup: Create a registered mother
            Mother mother = createMother(MOTHER_PHONE, "A", "3");
            
            // Setup: Create an available volunteer in the same zone
            Volunteer volunteer = createVolunteer(VOLUNTEER_PHONE, "Fatima", "A", SkillType.MIDWIFE, Set.of("3"));

            // Act: Send emergency request
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", MOTHER_PHONE)
                            .param("To", "+1555000000")
                            .param("Body", "EMERGENCY"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("EMERGENCY received")))
                    .andExpect(content().string(containsString("1 volunteer")));

            // Verify volunteer was alerted
            assertThat(mockSmsGateway.hasMessageTo(VOLUNTEER_PHONE)).isTrue();
            assertThat(mockSmsGateway.hasMessageContaining("EMERGENCY")).isTrue();
        }

        @Test
        @DisplayName("Should return error TwiML on processing error")
        void testIncomingSms_Error_ReturnsErrorTwiml() throws Exception {
            // Sending empty body should trigger validation error gracefully
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+1234567890")
                            .param("To", "+1555000000")
                            .param("Body", ""))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                    .andExpect(content().string(containsString("Response")));
        }

        @Test
        @DisplayName("Should return XML Content-Type")
        void testIncomingSms_ReturnsXmlContentType() throws Exception {
            mockMvc.perform(post("/api/sms/incoming")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("From", "+1234567890")
                            .param("To", "+1555000000")
                            .param("Body", "HELP"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
        }
    }

    @Nested
    @DisplayName("Simulator Endpoint Tests")
    class SimulatorTests {

        @Test
        @DisplayName("Should simulate SMS and return JSON response")
        void testSimulateSms_ReturnsJsonResponse() throws Exception {
            MvcResult result = mockMvc.perform(post("/api/sms/simulate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"from\": \"+1234567890\", \"body\": \"HELP\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.commandType").value("HELP"))
                    .andExpect(jsonPath("$.detectedLanguage").value("ENGLISH"))
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();
        }

        @Test
        @DisplayName("Should return parsed parameters in simulation")
        void testSimulateSms_ReturnsParsedParameters() throws Exception {
            mockMvc.perform(post("/api/sms/simulate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"from\": \"+1234567890\", \"body\": \"REG MOTHER CAMP B ZONE 5\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.commandType").value("REGISTER_MOTHER"))
                    .andExpect(jsonPath("$.parsedParameters.camp").value("B"))
                    .andExpect(jsonPath("$.parsedParameters.zone").value("5"));
        }

        @Test
        @DisplayName("Should detect Arabic language in simulation")
        void testSimulateSms_DetectsArabic() throws Exception {
            mockMvc.perform(post("/api/sms/simulate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"from\": \"+1234567890\", \"body\": \"طوارئ\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.commandType").value("EMERGENCY"))
                    .andExpect(jsonPath("$.detectedLanguage").value("ARABIC"));
        }
    }

    @Nested
    @DisplayName("Health Check Tests")
    class HealthCheckTests {

        @Test
        @DisplayName("Should return health status")
        void testHealthCheck() throws Exception {
            mockMvc.perform(get("/api/sms/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("SMS service is running"))
                    .andExpect(jsonPath("$.gatewayAvailable").value(true));
        }
    }

    // Helper methods

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

    private Volunteer createVolunteer(String phone, String name, String camp, SkillType skill, Set<String> zones) {
        Set<String> mutableZones = new HashSet<>(zones);
        
        Volunteer volunteer = Volunteer.builder()
                .phoneNumber(phone)
                .name(name)
                .camp(camp)
                .skillType(skill)
                .zones(mutableZones)
                .availability(AvailabilityStatus.AVAILABLE)
                .preferredLanguage(Language.ENGLISH)
                .registeredAt(LocalDateTime.now())
                .build();
        return volunteerRepository.save(volunteer);
    }
}
