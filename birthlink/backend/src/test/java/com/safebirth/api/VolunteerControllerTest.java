package com.safebirth.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safebirth.api.dto.AvailabilityUpdateRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for VolunteerController.
 * Tests all volunteer app endpoints with real database interactions.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS)
class VolunteerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MotherRepository motherRepository;

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    private static final String TEST_PHONE = "+962792222001";
    private static final String X_PHONE_HEADER = "X-Phone-Number";

    private Volunteer testVolunteer;
    private Mother testMother;
    private HelpRequest testRequest;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        helpRequestRepository.deleteAll();
        motherRepository.deleteAll();
        volunteerRepository.deleteAll();

        // Create test volunteer with mutable set for zones (required for Hibernate)
        testVolunteer = volunteerRepository.save(Volunteer.builder()
                .phoneNumber(TEST_PHONE)
                .name("Test Volunteer")
                .camp("CAMP-A")
                .skillType(SkillType.MIDWIFE)
                .zones(new HashSet<>(Set.of("ZONE-A", "ZONE-B")))
                .availability(AvailabilityStatus.AVAILABLE)
                .preferredLanguage(Language.ARABIC)
                .registeredAt(LocalDateTime.now())
                .completedCases(5)
                .build());

        // Create test mother
        testMother = motherRepository.save(Mother.builder()
                .phoneNumber("+962791111001")
                .name("Test Mother")
                .camp("CAMP-A")
                .zone("ZONE-A")
                .dueDate(LocalDate.now().plusDays(30))
                .riskLevel(RiskLevel.HIGH)
                .preferredLanguage(Language.ARABIC)
                .registeredAt(LocalDateTime.now())
                .build());

        // Create test help request assigned to volunteer
        testRequest = helpRequestRepository.save(HelpRequest.builder()
                .caseId("HR-0001")
                .mother(testMother)
                .requestType(RequestType.EMERGENCY)
                .status(RequestStatus.ACCEPTED)
                .zone("ZONE-A")
                .riskLevel(RiskLevel.HIGH)
                .dueDate(testMother.getDueDate())
                .createdAt(LocalDateTime.now())
                .acceptedBy(testVolunteer)
                .acceptedAt(LocalDateTime.now())
                .build());
    }

    @Nested
    @DisplayName("GET /api/volunteer/me")
    class GetProfileTests {

        @Test
        @DisplayName("Should return volunteer profile when found")
        void testGetProfile_Found() throws Exception {
            mockMvc.perform(get("/api/volunteer/me")
                            .header(X_PHONE_HEADER, TEST_PHONE)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Test Volunteer")))
                    .andExpect(jsonPath("$.skillType", is("MIDWIFE")))
                    .andExpect(jsonPath("$.availability", is("AVAILABLE")))
                    .andExpect(jsonPath("$.completedCases", is(5)))
                    .andExpect(jsonPath("$.zones", containsInAnyOrder("ZONE-A", "ZONE-B")));
        }

        @Test
        @DisplayName("Should return 404 when volunteer not found")
        void testGetProfile_NotFound_Returns404() throws Exception {
            mockMvc.perform(get("/api/volunteer/me")
                            .header(X_PHONE_HEADER, "+962799999999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error", is("Resource Not Found")));
        }

        @Test
        @DisplayName("Should return 400 when phone header is missing")
        void testGetProfile_MissingHeader_Returns400() throws Exception {
            mockMvc.perform(get("/api/volunteer/me")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Missing Required Header")));
        }

        @Test
        @DisplayName("Should mask phone number in response")
        void testGetProfile_MasksPhoneNumber() throws Exception {
            mockMvc.perform(get("/api/volunteer/me")
                            .header(X_PHONE_HEADER, TEST_PHONE)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.phoneNumber", endsWith("****")));
        }
    }

    @Nested
    @DisplayName("GET /api/volunteer/me/cases")
    class GetMyCasesTests {

        @Test
        @DisplayName("Should return cases assigned to volunteer")
        void testGetMyCases_ReturnsCasesForVolunteer() throws Exception {
            mockMvc.perform(get("/api/volunteer/me/cases")
                            .header(X_PHONE_HEADER, TEST_PHONE)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].caseId", is("HR-0001")))
                    .andExpect(jsonPath("$[0].status", is("ACCEPTED")))
                    .andExpect(jsonPath("$[0].requestType", is("EMERGENCY")));
        }

        @Test
        @DisplayName("Should return empty list when no cases assigned")
        void testGetMyCases_NoCases_ReturnsEmptyList() throws Exception {
            // Create a new volunteer with no cases
            Volunteer newVolunteer = volunteerRepository.save(Volunteer.builder()
                    .phoneNumber("+962792222002")
                    .name("New Volunteer")
                    .camp("CAMP-A")
                    .skillType(SkillType.NURSE)
                    .zones(new HashSet<>(Set.of("ZONE-A")))
                    .availability(AvailabilityStatus.AVAILABLE)
                    .preferredLanguage(Language.ARABIC)
                    .registeredAt(LocalDateTime.now())
                    .build());

            mockMvc.perform(get("/api/volunteer/me/cases")
                            .header(X_PHONE_HEADER, "+962792222002")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should only return active cases (not completed)")
        void testGetMyCases_OnlyActiveCases() throws Exception {
            // Mark the test request as completed
            testRequest.setStatus(RequestStatus.COMPLETED);
            testRequest.setClosedAt(LocalDateTime.now());
            helpRequestRepository.save(testRequest);

            mockMvc.perform(get("/api/volunteer/me/cases")
                            .header(X_PHONE_HEADER, TEST_PHONE)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 404 when volunteer not found")
        void testGetMyCases_VolunteerNotFound_Returns404() throws Exception {
            mockMvc.perform(get("/api/volunteer/me/cases")
                            .header(X_PHONE_HEADER, "+962799999999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/volunteer/me/availability")
    class UpdateAvailabilityTests {

        @Test
        @DisplayName("Should update availability status successfully")
        void testUpdateAvailability_Success() throws Exception {
            AvailabilityUpdateRequest request = new AvailabilityUpdateRequest(AvailabilityStatus.BUSY);

            mockMvc.perform(put("/api/volunteer/me/availability")
                            .header(X_PHONE_HEADER, TEST_PHONE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.availability", is("BUSY")));
        }

        @Test
        @DisplayName("Should return updated volunteer profile")
        void testUpdateAvailability_ReturnsUpdatedProfile() throws Exception {
            AvailabilityUpdateRequest request = new AvailabilityUpdateRequest(AvailabilityStatus.OFFLINE);

            mockMvc.perform(put("/api/volunteer/me/availability")
                            .header(X_PHONE_HEADER, TEST_PHONE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Test Volunteer")))
                    .andExpect(jsonPath("$.availability", is("OFFLINE")));
        }

        @Test
        @DisplayName("Should return 400 when availability is null")
        void testUpdateAvailability_NullStatus_Returns400() throws Exception {
            String invalidRequest = "{\"availability\": null}";

            mockMvc.perform(put("/api/volunteer/me/availability")
                            .header(X_PHONE_HEADER, TEST_PHONE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Validation Failed")));
        }

        @Test
        @DisplayName("Should return 400 when request body is invalid")
        void testUpdateAvailability_InvalidBody_Returns400() throws Exception {
            String invalidRequest = "{\"availability\": \"INVALID_STATUS\"}";

            mockMvc.perform(put("/api/volunteer/me/availability")
                            .header(X_PHONE_HEADER, TEST_PHONE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 when volunteer not found")
        void testUpdateAvailability_VolunteerNotFound_Returns404() throws Exception {
            AvailabilityUpdateRequest request = new AvailabilityUpdateRequest(AvailabilityStatus.BUSY);

            mockMvc.perform(put("/api/volunteer/me/availability")
                            .header(X_PHONE_HEADER, "+962799999999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when phone header is missing")
        void testUpdateAvailability_MissingHeader_Returns400() throws Exception {
            AvailabilityUpdateRequest request = new AvailabilityUpdateRequest(AvailabilityStatus.BUSY);

            mockMvc.perform(put("/api/volunteer/me/availability")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Missing Required Header")));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle multiple concurrent cases")
        void testGetMyCases_MultipleCases() throws Exception {
            // Create additional cases
            HelpRequest case2 = helpRequestRepository.save(HelpRequest.builder()
                    .caseId("HR-0002")
                    .mother(testMother)
                    .requestType(RequestType.SUPPORT)
                    .status(RequestStatus.IN_PROGRESS)
                    .zone("ZONE-A")
                    .createdAt(LocalDateTime.now())
                    .acceptedBy(testVolunteer)
                    .acceptedAt(LocalDateTime.now())
                    .build());

            mockMvc.perform(get("/api/volunteer/me/cases")
                            .header(X_PHONE_HEADER, TEST_PHONE)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("Should handle volunteer with all availability transitions")
        void testUpdateAvailability_AllTransitions() throws Exception {
            // AVAILABLE -> BUSY
            mockMvc.perform(put("/api/volunteer/me/availability")
                            .header(X_PHONE_HEADER, TEST_PHONE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new AvailabilityUpdateRequest(AvailabilityStatus.BUSY))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.availability", is("BUSY")));

            // BUSY -> OFFLINE
            mockMvc.perform(put("/api/volunteer/me/availability")
                            .header(X_PHONE_HEADER, TEST_PHONE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new AvailabilityUpdateRequest(AvailabilityStatus.OFFLINE))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.availability", is("OFFLINE")));

            // OFFLINE -> AVAILABLE
            mockMvc.perform(put("/api/volunteer/me/availability")
                            .header(X_PHONE_HEADER, TEST_PHONE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new AvailabilityUpdateRequest(AvailabilityStatus.AVAILABLE))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.availability", is("AVAILABLE")));
        }
    }
}
