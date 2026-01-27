package com.safebirth.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safebirth.domain.helprequest.HelpRequest;
import com.safebirth.domain.helprequest.HelpRequestRepository;
import com.safebirth.domain.helprequest.RequestStatus;
import com.safebirth.domain.helprequest.RequestType;
import com.safebirth.domain.mother.Language;
import com.safebirth.domain.mother.Mother;
import com.safebirth.domain.mother.MotherRepository;
import com.safebirth.domain.mother.RiskLevel;
import com.safebirth.domain.volunteer.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration tests for Dashboard API endpoints.
 * Tests complete workflows for NGO coordinator dashboard operations.
 * 
 * These tests verify:
 * - Dashboard statistics aggregation
 * - Case listing with filters and pagination
 * - Volunteer listing with filters
 * - Zone statistics
 * - Volunteer profile operations
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DashboardApiIntegrationTest {

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

    @BeforeEach
    void setUp() {
        // Clear existing data for clean tests
        helpRequestRepository.deleteAll();
        motherRepository.deleteAll();
        volunteerRepository.deleteAll();
    }

    // ==================== Dashboard Stats Tests ====================

    @Nested
    @DisplayName("GET /api/dashboard/stats")
    class DashboardStatsTests {

        @Test
        @DisplayName("Should return all dashboard metrics")
        void testGetStats_ReturnsAllMetrics() throws Exception {
            // Setup: Create test data across multiple zones
            createTestData();

            mockMvc.perform(get("/api/dashboard/stats")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalMothers", is(3)))
                    .andExpect(jsonPath("$.totalVolunteers", is(2)))
                    .andExpect(jsonPath("$.availableVolunteers", is(1)))
                    .andExpect(jsonPath("$.pendingRequests", is(2)))
                    .andExpect(jsonPath("$.pendingEmergencies", is(1)))
                    .andExpect(jsonPath("$.highRiskMothers", is(1)))
                    .andExpect(jsonPath("$.mothersByZone").isMap())
                    .andExpect(jsonPath("$.requestsByStatus").isMap())
                    .andExpect(jsonPath("$.volunteersBySkill").isMap());
        }

        @Test
        @DisplayName("Should return distribution maps correctly")
        void testGetStats_DistributionMaps() throws Exception {
            createTestData();

            MvcResult result = mockMvc.perform(get("/api/dashboard/stats")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
            
            // Verify mothersByZone has entries
            assertThat(json.has("mothersByZone")).isTrue();
            assertThat(json.get("mothersByZone").size()).isGreaterThanOrEqualTo(1);
            
            // Verify requestsByStatus has entries
            assertThat(json.has("requestsByStatus")).isTrue();
            assertThat(json.get("requestsByStatus").has("PENDING")).isTrue();
            
            // Verify volunteersBySkill has entries
            assertThat(json.has("volunteersBySkill")).isTrue();
        }

        @Test
        @DisplayName("Should include upcoming due dates within 30 days")
        void testGetStats_UpcomingDueDates() throws Exception {
            // Create mother with due date in 7 days
            Mother mother = createMother("+100000001", "A", "1", 
                    LocalDate.now().plusDays(7), RiskLevel.MEDIUM);
            
            mockMvc.perform(get("/api/dashboard/stats")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.upcomingDueDates").isArray())
                    .andExpect(jsonPath("$.upcomingDueDates", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        @DisplayName("Should return zero counts when database is empty")
        void testGetStats_EmptyDatabase() throws Exception {
            mockMvc.perform(get("/api/dashboard/stats")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalMothers", is(0)))
                    .andExpect(jsonPath("$.totalVolunteers", is(0)))
                    .andExpect(jsonPath("$.pendingRequests", is(0)))
                    .andExpect(jsonPath("$.pendingEmergencies", is(0)));
        }

        @Test
        @DisplayName("Should count active vs completed requests correctly")
        void testGetStats_RequestStatusCounts() throws Exception {
            Mother mother = createMother("+100000002", "A", "1", null, RiskLevel.LOW);
            
            // Create 2 pending, 1 accepted, 1 completed requests
            createHelpRequest(mother, "HR-001", RequestStatus.PENDING, RequestType.EMERGENCY);
            createHelpRequest(mother, "HR-002", RequestStatus.PENDING, RequestType.SUPPORT);
            createHelpRequest(mother, "HR-003", RequestStatus.ACCEPTED, RequestType.SUPPORT);
            createHelpRequest(mother, "HR-004", RequestStatus.COMPLETED, RequestType.EMERGENCY);

            mockMvc.perform(get("/api/dashboard/stats")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pendingRequests", is(2)))
                    .andExpect(jsonPath("$.activeRequests", is(1)))
                    .andExpect(jsonPath("$.completedRequests", is(1)));
        }
    }

    // ==================== Cases List Tests ====================

    @Nested
    @DisplayName("GET /api/dashboard/cases")
    class CasesListTests {

        @Test
        @DisplayName("Should return all cases without filter")
        void testGetCases_NoFilter() throws Exception {
            createTestData();

            mockMvc.perform(get("/api/dashboard/cases")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[*].caseId", hasItems("HR-0001", "HR-0002", "HR-0003")));
        }

        @Test
        @DisplayName("Should filter cases by zone")
        void testGetCases_FilterByZone() throws Exception {
            createTestData();

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
            createTestData();

            mockMvc.perform(get("/api/dashboard/cases")
                            .param("status", "PENDING")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].status", everyItem(is("PENDING"))));
        }

        @Test
        @DisplayName("Should support pagination")
        void testGetCases_Pagination() throws Exception {
            // Create 10 cases
            Mother mother = createMother("+111111111", "A", "1", null, RiskLevel.LOW);
            for (int i = 1; i <= 10; i++) {
                createHelpRequest(mother, "HR-PAGE-" + String.format("%03d", i), 
                        RequestStatus.PENDING, RequestType.SUPPORT);
            }

            // Get first page (size 3)
            mockMvc.perform(get("/api/dashboard/cases")
                            .param("page", "0")
                            .param("size", "3")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)));

            // Get second page
            mockMvc.perform(get("/api/dashboard/cases")
                            .param("page", "1")
                            .param("size", "3")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)));
        }

        @Test
        @DisplayName("Should combine zone and status filters")
        void testGetCases_CombinedFilters() throws Exception {
            createTestData();

            mockMvc.perform(get("/api/dashboard/cases")
                            .param("zone", "ZONE-B")
                            .param("status", "PENDING")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].zone", is("ZONE-B")))
                    .andExpect(jsonPath("$[0].status", is("PENDING")));
        }

        @Test
        @DisplayName("Should return empty array when no matches")
        void testGetCases_NoMatches() throws Exception {
            createTestData();

            mockMvc.perform(get("/api/dashboard/cases")
                            .param("zone", "ZONE-NONEXISTENT")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // ==================== Single Case Tests ====================

    @Nested
    @DisplayName("GET /api/dashboard/cases/{caseId}")
    class SingleCaseTests {

        @Test
        @DisplayName("Should return case details when found")
        void testGetCase_Found() throws Exception {
            Mother mother = createMother("+122222222", "A", "1", 
                    LocalDate.now().plusDays(14), RiskLevel.HIGH);
            createHelpRequest(mother, "HR-DETAIL-001", RequestStatus.PENDING, RequestType.EMERGENCY);

            mockMvc.perform(get("/api/dashboard/cases/HR-DETAIL-001")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.caseId", is("HR-DETAIL-001")))
                    .andExpect(jsonPath("$.requestType", is("EMERGENCY")))
                    .andExpect(jsonPath("$.status", is("PENDING")))
                    .andExpect(jsonPath("$.zone", is("1")))
                    .andExpect(jsonPath("$.riskLevel", is("HIGH")));
        }

        @Test
        @DisplayName("Should return 404 when case not found")
        void testGetCase_NotFound() throws Exception {
            mockMvc.perform(get("/api/dashboard/cases/HR-NONEXISTENT")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error", is("Resource Not Found")));
        }

        @Test
        @DisplayName("Should normalize case ID format")
        void testGetCase_NormalizesId() throws Exception {
            Mother mother = createMother("+133333333", "B", "2", null, RiskLevel.LOW);
            createHelpRequest(mother, "HR-0099", RequestStatus.PENDING, RequestType.SUPPORT);

            // Test with just numbers (should be normalized to HR-0099)
            mockMvc.perform(get("/api/dashboard/cases/0099")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.caseId", is("HR-0099")));
        }

        @Test
        @DisplayName("Should include volunteer info when case is accepted")
        void testGetCase_IncludesVolunteerInfo() throws Exception {
            Mother mother = createMother("+144444444", "A", "1", null, RiskLevel.MEDIUM);
            Volunteer volunteer = createVolunteer("+144444445", "Assigned Vol", "A", 
                    SkillType.MIDWIFE, Set.of("1"), AvailabilityStatus.BUSY);
            
            HelpRequest request = createHelpRequest(mother, "HR-ASSIGNED-001", 
                    RequestStatus.ACCEPTED, RequestType.EMERGENCY);
            request.setAcceptedBy(volunteer);
            request.setAcceptedAt(LocalDateTime.now());
            helpRequestRepository.save(request);

            mockMvc.perform(get("/api/dashboard/cases/HR-ASSIGNED-001")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("ACCEPTED")))
                    .andExpect(jsonPath("$.volunteerName", is("Assigned Vol")));
        }
    }

    // ==================== Volunteers List Tests ====================

    @Nested
    @DisplayName("GET /api/dashboard/volunteers")
    class VolunteersListTests {

        @Test
        @DisplayName("Should return all volunteers without filter")
        void testGetVolunteers_NoFilter() throws Exception {
            createVolunteer("+200000001", "Vol1", "A", SkillType.MIDWIFE, 
                    Set.of("1"), AvailabilityStatus.AVAILABLE);
            createVolunteer("+200000002", "Vol2", "A", SkillType.NURSE, 
                    Set.of("2"), AvailabilityStatus.BUSY);

            mockMvc.perform(get("/api/dashboard/volunteers")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].name", hasItems("Vol1", "Vol2")));
        }

        @Test
        @DisplayName("Should filter volunteers by availability")
        void testGetVolunteers_FilterByAvailability() throws Exception {
            createVolunteer("+200000003", "Available Vol", "A", SkillType.MIDWIFE, 
                    Set.of("1"), AvailabilityStatus.AVAILABLE);
            createVolunteer("+200000004", "Busy Vol", "A", SkillType.NURSE, 
                    Set.of("1"), AvailabilityStatus.BUSY);
            createVolunteer("+200000005", "Offline Vol", "A", SkillType.TRAINED_ATTENDANT, 
                    Set.of("1"), AvailabilityStatus.OFFLINE);

            mockMvc.perform(get("/api/dashboard/volunteers")
                            .param("availability", "AVAILABLE")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name", is("Available Vol")));
        }

        @Test
        @DisplayName("Should filter volunteers by zone")
        void testGetVolunteers_FilterByZone() throws Exception {
            createVolunteer("+200000006", "Zone1 Vol", "A", SkillType.MIDWIFE, 
                    Set.of("1", "2"), AvailabilityStatus.AVAILABLE);
            createVolunteer("+200000007", "Zone3 Vol", "A", SkillType.NURSE, 
                    Set.of("3"), AvailabilityStatus.AVAILABLE);

            mockMvc.perform(get("/api/dashboard/volunteers")
                            .param("zone", "1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name", is("Zone1 Vol")));
        }

        @Test
        @DisplayName("Should include volunteer statistics")
        void testGetVolunteers_IncludesStats() throws Exception {
            Volunteer volunteer = createVolunteer("+200000008", "Stats Vol", "A", 
                    SkillType.MIDWIFE, Set.of("1"), AvailabilityStatus.AVAILABLE);
            volunteer.setCompletedCases(25);
            volunteerRepository.save(volunteer);

            mockMvc.perform(get("/api/dashboard/volunteers")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].completedCases", is(25)));
        }
    }

    // ==================== Zone Stats Tests ====================

    @Nested
    @DisplayName("GET /api/dashboard/zones")
    class ZoneStatsTests {

        @Test
        @DisplayName("Should return statistics for all zones")
        void testGetZoneStats() throws Exception {
            createTestData();

            mockMvc.perform(get("/api/dashboard/zones")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                    .andExpect(jsonPath("$[*].zone", notNullValue()))
                    .andExpect(jsonPath("$[*].motherCount", notNullValue()))
                    .andExpect(jsonPath("$[*].volunteerCount", notNullValue()));
        }

        @Test
        @DisplayName("Should include pending emergencies count per zone")
        void testGetZoneStats_PendingEmergencies() throws Exception {
            Mother motherA = createMother("+300000001", "A", "ZONE-A", null, RiskLevel.HIGH);
            createHelpRequest(motherA, "HR-ZONE-001", RequestStatus.PENDING, RequestType.EMERGENCY);
            
            Mother motherB = createMother("+300000002", "A", "ZONE-B", null, RiskLevel.LOW);
            createHelpRequest(motherB, "HR-ZONE-002", RequestStatus.PENDING, RequestType.SUPPORT);

            mockMvc.perform(get("/api/dashboard/zones")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.zone == 'ZONE-A')].pendingEmergencies", 
                            contains(1)));
        }
    }

    // ==================== Volunteer Profile Tests ====================

    @Nested
    @DisplayName("Volunteer API Tests")
    class VolunteerApiTests {

        @Test
        @DisplayName("Should return volunteer profile with valid phone header")
        void testGetVolunteerMe_Success() throws Exception {
            Volunteer volunteer = createVolunteer("+400000001", "My Vol", "A", 
                    SkillType.MIDWIFE, Set.of("1", "2"), AvailabilityStatus.AVAILABLE);
            volunteer.setCompletedCases(5);
            volunteerRepository.save(volunteer);

            mockMvc.perform(get("/api/volunteer/me")
                            .header("X-Phone-Number", "+400000001")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("My Vol")))
                    .andExpect(jsonPath("$.skillType", is("MIDWIFE")))
                    .andExpect(jsonPath("$.availability", is("AVAILABLE")))
                    .andExpect(jsonPath("$.completedCases", is(5)));
        }

        @Test
        @DisplayName("Should return 404 when volunteer not found")
        void testGetVolunteerMe_NotFound() throws Exception {
            mockMvc.perform(get("/api/volunteer/me")
                            .header("X-Phone-Number", "+400000099")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when phone header is missing")
        void testGetVolunteerMe_MissingHeader() throws Exception {
            mockMvc.perform(get("/api/volunteer/me")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should update volunteer availability")
        void testUpdateAvailability_Success() throws Exception {
            createVolunteer("+400000002", "Avail Vol", "A", SkillType.NURSE, 
                    Set.of("1"), AvailabilityStatus.AVAILABLE);

            mockMvc.perform(put("/api/volunteer/me/availability")
                            .header("X-Phone-Number", "+400000002")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"availability\": \"BUSY\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.availability", is("BUSY")));

            // Verify update persisted
            var updated = volunteerRepository.findByPhoneNumber("+400000002");
            assertThat(updated).isPresent();
            assertThat(updated.get().getAvailability()).isEqualTo(AvailabilityStatus.BUSY);
        }

        @Test
        @DisplayName("Should return volunteer's cases")
        void testGetVolunteerCases() throws Exception {
            Mother mother = createMother("+400000003", "A", "1", null, RiskLevel.LOW);
            Volunteer volunteer = createVolunteer("+400000004", "Case Vol", "A", 
                    SkillType.MIDWIFE, Set.of("1"), AvailabilityStatus.BUSY);
            
            HelpRequest request = createHelpRequest(mother, "HR-VOL-001", 
                    RequestStatus.ACCEPTED, RequestType.SUPPORT);
            request.setAcceptedBy(volunteer);
            helpRequestRepository.save(request);

            mockMvc.perform(get("/api/volunteer/me/cases")
                            .header("X-Phone-Number", "+400000004")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].caseId", is("HR-VOL-001")));
        }
    }

    // ==================== Helper Methods ====================

    private void createTestData() {
        // Create 3 mothers in different zones
        Mother mother1 = createMother("+962791111001", "A", "ZONE-A", 
                LocalDate.now().plusDays(30), RiskLevel.HIGH);
        Mother mother2 = createMother("+962791111002", "A", "ZONE-B", 
                LocalDate.now().plusDays(45), RiskLevel.MEDIUM);
        Mother mother3 = createMother("+962791111003", "A", "ZONE-B", 
                null, RiskLevel.LOW);

        // Create 2 volunteers
        Volunteer vol1 = createVolunteer("+962792222001", "Volunteer One", "A", 
                SkillType.MIDWIFE, Set.of("ZONE-A", "ZONE-B"), AvailabilityStatus.AVAILABLE);
        Volunteer vol2 = createVolunteer("+962792222002", "Volunteer Two", "A", 
                SkillType.NURSE, Set.of("ZONE-B"), AvailabilityStatus.BUSY);

        // Create 3 help requests
        createHelpRequest(mother1, "HR-0001", RequestStatus.PENDING, RequestType.EMERGENCY);
        createHelpRequest(mother2, "HR-0002", RequestStatus.PENDING, RequestType.SUPPORT);
        HelpRequest acceptedRequest = createHelpRequest(mother3, "HR-0003", 
                RequestStatus.ACCEPTED, RequestType.SUPPORT);
        acceptedRequest.setAcceptedBy(vol2);
        helpRequestRepository.save(acceptedRequest);
    }

    private Mother createMother(String phone, String camp, String zone, 
                                 LocalDate dueDate, RiskLevel risk) {
        Mother mother = Mother.builder()
                .phoneNumber(phone)
                .camp(camp)
                .zone(zone)
                .dueDate(dueDate)
                .riskLevel(risk)
                .preferredLanguage(Language.ARABIC)
                .registeredAt(LocalDateTime.now())
                .build();
        return motherRepository.save(mother);
    }

    private Volunteer createVolunteer(String phone, String name, String camp, 
                                       SkillType skill, Set<String> zones,
                                       AvailabilityStatus availability) {
        Volunteer volunteer = Volunteer.builder()
                .phoneNumber(phone)
                .name(name)
                .camp(camp)
                .skillType(skill)
                .zones(new HashSet<>(zones))
                .availability(availability)
                .preferredLanguage(Language.ARABIC)
                .registeredAt(LocalDateTime.now())
                .completedCases(0)
                .build();
        return volunteerRepository.save(volunteer);
    }

    private HelpRequest createHelpRequest(Mother mother, String caseId, 
                                           RequestStatus status, RequestType type) {
        HelpRequest request = HelpRequest.builder()
                .caseId(caseId)
                .mother(mother)
                .requestType(type)
                .status(status)
                .zone(mother.getZone())
                .riskLevel(mother.getRiskLevel())
                .dueDate(mother.getDueDate())
                .createdAt(LocalDateTime.now())
                .build();
        
        if (status == RequestStatus.COMPLETED) {
            request.setClosedAt(LocalDateTime.now());
        }
        
        return helpRequestRepository.save(request);
    }
}
