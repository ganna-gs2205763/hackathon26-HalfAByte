package com.safebirth.api;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for DashboardController.
 * Tests all dashboard endpoints with real database interactions.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MotherRepository motherRepository;

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    private Mother testMother;
    private Volunteer testVolunteer;
    private HelpRequest testRequest;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        helpRequestRepository.deleteAll();
        motherRepository.deleteAll();
        volunteerRepository.deleteAll();

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

        // Create test volunteer with mutable set for zones (required for Hibernate)
        testVolunteer = volunteerRepository.save(Volunteer.builder()
                .phoneNumber("+962792222001")
                .name("Test Volunteer")
                .camp("CAMP-A")
                .skillType(SkillType.MIDWIFE)
                .zones(new HashSet<>(Set.of("ZONE-A", "ZONE-B")))
                .availability(AvailabilityStatus.AVAILABLE)
                .preferredLanguage(Language.ARABIC)
                .registeredAt(LocalDateTime.now())
                .completedCases(5)
                .build());

        // Create test help request
        testRequest = helpRequestRepository.save(HelpRequest.builder()
                .caseId("HR-0001")
                .mother(testMother)
                .requestType(RequestType.EMERGENCY)
                .status(RequestStatus.PENDING)
                .zone("ZONE-A")
                .riskLevel(RiskLevel.HIGH)
                .dueDate(testMother.getDueDate())
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Nested
    @DisplayName("GET /api/dashboard/stats")
    class GetStatsTests {

        @Test
        @DisplayName("Should return all dashboard metrics")
        void testGetStats_ReturnsAllMetrics() throws Exception {
            mockMvc.perform(get("/api/dashboard/stats")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalMothers", is(1)))
                    .andExpect(jsonPath("$.totalVolunteers", is(1)))
                    .andExpect(jsonPath("$.availableVolunteers", is(1)))
                    .andExpect(jsonPath("$.pendingRequests", is(1)))
                    .andExpect(jsonPath("$.pendingEmergencies", is(1)))
                    .andExpect(jsonPath("$.highRiskMothers", is(1)))
                    .andExpect(jsonPath("$.mothersByZone").isMap())
                    .andExpect(jsonPath("$.mothersByZone.ZONE-A", is(1)))
                    .andExpect(jsonPath("$.requestsByStatus").isMap())
                    .andExpect(jsonPath("$.volunteersBySkill").isMap());
        }

        @Test
        @DisplayName("Should include upcoming due dates within 30 days")
        void testGetStats_IncludesUpcomingDueDates() throws Exception {
            mockMvc.perform(get("/api/dashboard/stats")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.upcomingDueDates", hasSize(1)))
                    .andExpect(jsonPath("$.upcomingDueDates[0].count", is(1)));
        }

        @Test
        @DisplayName("Should return zero counts when database is empty")
        void testGetStats_EmptyDatabase() throws Exception {
            helpRequestRepository.deleteAll();
            motherRepository.deleteAll();
            volunteerRepository.deleteAll();

            mockMvc.perform(get("/api/dashboard/stats")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalMothers", is(0)))
                    .andExpect(jsonPath("$.totalVolunteers", is(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/dashboard/cases")
    class GetCasesTests {

        @Test
        @DisplayName("Should return all cases without filter")
        void testGetCases_NoFilter_ReturnsAll() throws Exception {
            mockMvc.perform(get("/api/dashboard/cases")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].caseId", is("HR-0001")))
                    .andExpect(jsonPath("$[0].status", is("PENDING")))
                    .andExpect(jsonPath("$[0].requestType", is("EMERGENCY")));
        }

        @Test
        @DisplayName("Should filter cases by zone")
        void testGetCases_FilterByZone() throws Exception {
            // Create another case in a different zone
            Mother mother2 = motherRepository.save(Mother.builder()
                    .phoneNumber("+962791111002")
                    .camp("CAMP-A")
                    .zone("ZONE-B")
                    .preferredLanguage(Language.ARABIC)
                    .registeredAt(LocalDateTime.now())
                    .build());
            
            helpRequestRepository.save(HelpRequest.builder()
                    .caseId("HR-0002")
                    .mother(mother2)
                    .requestType(RequestType.SUPPORT)
                    .status(RequestStatus.PENDING)
                    .zone("ZONE-B")
                    .createdAt(LocalDateTime.now())
                    .build());

            mockMvc.perform(get("/api/dashboard/cases")
                            .param("zone", "ZONE-A")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].zone", is("ZONE-A")));
        }

        @Test
        @DisplayName("Should filter cases by status")
        void testGetCases_FilterByStatus() throws Exception {
            // Create a completed case
            HelpRequest completedRequest = helpRequestRepository.save(HelpRequest.builder()
                    .caseId("HR-0002")
                    .mother(testMother)
                    .requestType(RequestType.SUPPORT)
                    .status(RequestStatus.COMPLETED)
                    .zone("ZONE-A")
                    .createdAt(LocalDateTime.now())
                    .closedAt(LocalDateTime.now())
                    .build());

            mockMvc.perform(get("/api/dashboard/cases")
                            .param("status", "PENDING")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].status", is("PENDING")));
        }

        @Test
        @DisplayName("Should support pagination")
        void testGetCases_Pagination() throws Exception {
            // Create additional cases
            for (int i = 2; i <= 5; i++) {
                helpRequestRepository.save(HelpRequest.builder()
                        .caseId("HR-000" + i)
                        .mother(testMother)
                        .requestType(RequestType.SUPPORT)
                        .status(RequestStatus.PENDING)
                        .zone("ZONE-A")
                        .createdAt(LocalDateTime.now().minusMinutes(i))
                        .build());
            }

            mockMvc.perform(get("/api/dashboard/cases")
                            .param("page", "0")
                            .param("size", "2")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }
    }

    @Nested
    @DisplayName("GET /api/dashboard/cases/{caseId}")
    class GetCaseByIdTests {

        @Test
        @DisplayName("Should return case when found")
        void testGetCase_Found() throws Exception {
            mockMvc.perform(get("/api/dashboard/cases/HR-0001")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.caseId", is("HR-0001")))
                    .andExpect(jsonPath("$.requestType", is("EMERGENCY")))
                    .andExpect(jsonPath("$.status", is("PENDING")))
                    .andExpect(jsonPath("$.zone", is("ZONE-A")))
                    .andExpect(jsonPath("$.riskLevel", is("HIGH")));
        }

        @Test
        @DisplayName("Should return 404 when case not found")
        void testGetCase_NotFound_Returns404() throws Exception {
            mockMvc.perform(get("/api/dashboard/cases/HR-9999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error", is("Resource Not Found")));
        }

        @Test
        @DisplayName("Should normalize case ID format")
        void testGetCase_NormalizesId() throws Exception {
            // Test with just numbers
            mockMvc.perform(get("/api/dashboard/cases/0001")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.caseId", is("HR-0001")));
        }
    }

    @Nested
    @DisplayName("GET /api/dashboard/volunteers")
    class GetVolunteersTests {

        @Test
        @DisplayName("Should return all volunteers without filter")
        void testGetVolunteers_NoFilter() throws Exception {
            mockMvc.perform(get("/api/dashboard/volunteers")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name", is("Test Volunteer")))
                    .andExpect(jsonPath("$[0].skillType", is("MIDWIFE")))
                    .andExpect(jsonPath("$[0].availability", is("AVAILABLE")));
        }

        @Test
        @DisplayName("Should filter volunteers by availability")
        void testGetVolunteers_FilterByAvailability() throws Exception {
            // Create a busy volunteer
            volunteerRepository.save(Volunteer.builder()
                    .phoneNumber("+962792222002")
                    .name("Busy Volunteer")
                    .camp("CAMP-A")
                    .skillType(SkillType.NURSE)
                    .zones(new HashSet<>(Set.of("ZONE-A")))
                    .availability(AvailabilityStatus.BUSY)
                    .preferredLanguage(Language.ARABIC)
                    .registeredAt(LocalDateTime.now())
                    .build());

            mockMvc.perform(get("/api/dashboard/volunteers")
                            .param("availability", "AVAILABLE")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].availability", is("AVAILABLE")));
        }

        @Test
        @DisplayName("Should filter volunteers by zone")
        void testGetVolunteers_FilterByZone() throws Exception {
            // Create a volunteer in a different zone
            volunteerRepository.save(Volunteer.builder()
                    .phoneNumber("+962792222003")
                    .name("Zone C Volunteer")
                    .camp("CAMP-A")
                    .skillType(SkillType.NURSE)
                    .zones(new HashSet<>(Set.of("ZONE-C")))
                    .availability(AvailabilityStatus.AVAILABLE)
                    .preferredLanguage(Language.ARABIC)
                    .registeredAt(LocalDateTime.now())
                    .build());

            mockMvc.perform(get("/api/dashboard/volunteers")
                            .param("zone", "ZONE-A")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("GET /api/dashboard/zones")
    class GetZoneStatsTests {

        @Test
        @DisplayName("Should return statistics for all zones")
        void testGetZoneStats() throws Exception {
            mockMvc.perform(get("/api/dashboard/zones")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$[0].zone", notNullValue()))
                    .andExpect(jsonPath("$[0].motherCount", notNullValue()))
                    .andExpect(jsonPath("$[0].volunteerCount", notNullValue()));
        }

        @Test
        @DisplayName("Should include pending emergencies count")
        void testGetZoneStats_IncludesPendingEmergencies() throws Exception {
            mockMvc.perform(get("/api/dashboard/zones")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.zone == 'ZONE-A')].pendingEmergencies", 
                            contains(1)));
        }
    }
}
