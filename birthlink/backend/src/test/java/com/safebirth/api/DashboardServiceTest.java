package com.safebirth.api;

import com.safebirth.api.dto.DashboardStatsDto;
import com.safebirth.api.dto.ZoneStatsDto;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DashboardService.
 * Tests statistics aggregation logic.
 */
@SpringBootTest
@Transactional
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS)
class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private MotherRepository motherRepository;

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    private int phoneCounter = 0;

    @BeforeEach
    void setUp() {
        // Clear existing data
        helpRequestRepository.deleteAll();
        motherRepository.deleteAll();
        volunteerRepository.deleteAll();
        phoneCounter = 0;
    }

    private String generatePhoneNumber() {
        phoneCounter++;
        return String.format("+9627911%05d", phoneCounter);
    }

    @Nested
    @DisplayName("getStats()")
    class GetStatsTests {

        @Test
        @DisplayName("Should return zero counts when database is empty")
        void testGetStats_EmptyDatabase() {
            DashboardStatsDto stats = dashboardService.getStats();

            assertThat(stats.totalMothers()).isZero();
            assertThat(stats.totalVolunteers()).isZero();
            assertThat(stats.availableVolunteers()).isZero();
            assertThat(stats.activeRequests()).isZero();
            assertThat(stats.pendingRequests()).isZero();
            assertThat(stats.pendingEmergencies()).isZero();
            assertThat(stats.completedToday()).isZero();
            assertThat(stats.highRiskMothers()).isZero();
        }

        @Test
        @DisplayName("Should correctly count mothers by zone")
        void testGetStats_MothersByZone() {
            createMother("ZONE-A", RiskLevel.LOW);
            createMother("ZONE-A", RiskLevel.MEDIUM);
            createMother("ZONE-B", RiskLevel.HIGH);

            DashboardStatsDto stats = dashboardService.getStats();

            assertThat(stats.mothersByZone()).containsEntry("ZONE-A", 2L);
            assertThat(stats.mothersByZone()).containsEntry("ZONE-B", 1L);
        }

        @Test
        @DisplayName("Should correctly count high-risk mothers")
        void testGetStats_HighRiskMothers() {
            createMother("ZONE-A", RiskLevel.HIGH);
            createMother("ZONE-A", RiskLevel.HIGH);
            createMother("ZONE-A", RiskLevel.LOW);

            DashboardStatsDto stats = dashboardService.getStats();

            assertThat(stats.highRiskMothers()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should correctly count volunteers by skill")
        void testGetStats_VolunteersBySkill() {
            createVolunteer(SkillType.MIDWIFE, AvailabilityStatus.AVAILABLE);
            createVolunteer(SkillType.MIDWIFE, AvailabilityStatus.BUSY);
            createVolunteer(SkillType.NURSE, AvailabilityStatus.AVAILABLE);

            DashboardStatsDto stats = dashboardService.getStats();

            assertThat(stats.volunteersBySkill()).containsEntry("MIDWIFE", 2L);
            assertThat(stats.volunteersBySkill()).containsEntry("NURSE", 1L);
        }

        @Test
        @DisplayName("Should correctly count available volunteers")
        void testGetStats_AvailableVolunteers() {
            createVolunteer(SkillType.MIDWIFE, AvailabilityStatus.AVAILABLE);
            createVolunteer(SkillType.NURSE, AvailabilityStatus.AVAILABLE);
            createVolunteer(SkillType.NURSE, AvailabilityStatus.BUSY);
            createVolunteer(SkillType.NURSE, AvailabilityStatus.OFFLINE);

            DashboardStatsDto stats = dashboardService.getStats();

            assertThat(stats.totalVolunteers()).isEqualTo(4);
            assertThat(stats.availableVolunteers()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should correctly count requests by status")
        void testGetStats_RequestsByStatus() {
            Mother mother = createMother("ZONE-A", RiskLevel.LOW);
            createHelpRequest(mother, RequestStatus.PENDING, RequestType.EMERGENCY);
            createHelpRequest(mother, RequestStatus.PENDING, RequestType.SUPPORT);
            createHelpRequest(mother, RequestStatus.ACCEPTED, RequestType.EMERGENCY);
            createHelpRequest(mother, RequestStatus.IN_PROGRESS, RequestType.SUPPORT);
            createHelpRequest(mother, RequestStatus.COMPLETED, RequestType.SUPPORT);

            DashboardStatsDto stats = dashboardService.getStats();

            assertThat(stats.pendingRequests()).isEqualTo(2);
            assertThat(stats.activeRequests()).isEqualTo(2); // ACCEPTED + IN_PROGRESS
            assertThat(stats.requestsByStatus()).containsEntry("PENDING", 2L);
            assertThat(stats.requestsByStatus()).containsEntry("ACCEPTED", 1L);
            assertThat(stats.requestsByStatus()).containsEntry("IN_PROGRESS", 1L);
            assertThat(stats.requestsByStatus()).containsEntry("COMPLETED", 1L);
        }

        @Test
        @DisplayName("Should correctly count pending emergencies")
        void testGetStats_PendingEmergencies() {
            Mother mother = createMother("ZONE-A", RiskLevel.HIGH);
            createHelpRequest(mother, RequestStatus.PENDING, RequestType.EMERGENCY);
            createHelpRequest(mother, RequestStatus.PENDING, RequestType.EMERGENCY);
            createHelpRequest(mother, RequestStatus.PENDING, RequestType.SUPPORT);
            createHelpRequest(mother, RequestStatus.ACCEPTED, RequestType.EMERGENCY);

            DashboardStatsDto stats = dashboardService.getStats();

            assertThat(stats.pendingEmergencies()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should correctly count completed today")
        void testGetStats_CompletedToday() {
            Mother mother = createMother("ZONE-A", RiskLevel.LOW);
            
            // Completed today
            HelpRequest todayCompleted = createHelpRequest(mother, RequestStatus.COMPLETED, RequestType.SUPPORT);
            todayCompleted.setClosedAt(LocalDateTime.now());
            helpRequestRepository.save(todayCompleted);

            // Completed yesterday
            HelpRequest yesterdayCompleted = createHelpRequest(mother, RequestStatus.COMPLETED, RequestType.SUPPORT);
            yesterdayCompleted.setClosedAt(LocalDateTime.now().minusDays(1));
            helpRequestRepository.save(yesterdayCompleted);

            DashboardStatsDto stats = dashboardService.getStats();

            assertThat(stats.completedToday()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should include upcoming due dates within 30 days")
        void testGetStats_UpcomingDueDates() {
            // Within 30 days
            Mother m1 = createMotherWithDueDate("ZONE-A", LocalDate.now().plusDays(7));
            Mother m2 = createMotherWithDueDate("ZONE-A", LocalDate.now().plusDays(7)); // Same date
            Mother m3 = createMotherWithDueDate("ZONE-A", LocalDate.now().plusDays(14));
            
            // Outside 30 days
            Mother m4 = createMotherWithDueDate("ZONE-A", LocalDate.now().plusDays(45));
            
            // Past due date
            Mother m5 = createMotherWithDueDate("ZONE-A", LocalDate.now().minusDays(5));

            DashboardStatsDto stats = dashboardService.getStats();

            assertThat(stats.upcomingDueDates()).hasSize(2); // 2 distinct dates
            assertThat(stats.upcomingDueDates())
                    .extracting(DashboardStatsDto.DueDateCluster::count)
                    .containsExactly(2L, 1L); // 2 on first date, 1 on second
        }
    }

    @Nested
    @DisplayName("getZoneStats()")
    class GetZoneStatsTests {

        @Test
        @DisplayName("Should return stats for all zones")
        void testGetZoneStats_AllZones() {
            createMother("ZONE-A", RiskLevel.LOW);
            createMother("ZONE-A", RiskLevel.HIGH);
            createMother("ZONE-B", RiskLevel.MEDIUM);

            List<ZoneStatsDto> zoneStats = dashboardService.getZoneStats();

            assertThat(zoneStats).hasSize(2);
            assertThat(zoneStats)
                    .extracting(ZoneStatsDto::zone)
                    .containsExactlyInAnyOrder("ZONE-A", "ZONE-B");
        }

        @Test
        @DisplayName("Should correctly aggregate zone-level metrics")
        void testGetZoneStats_Aggregation() {
            createMother("ZONE-A", RiskLevel.LOW);
            createMother("ZONE-A", RiskLevel.HIGH);

            Volunteer v = volunteerRepository.save(Volunteer.builder()
                    .phoneNumber("+962792222001")
                    .name("Test Volunteer")
                    .camp("CAMP-A")
                    .skillType(SkillType.MIDWIFE)
                    .zones(Set.of("ZONE-A"))
                    .availability(AvailabilityStatus.AVAILABLE)
                    .preferredLanguage(Language.ARABIC)
                    .registeredAt(LocalDateTime.now())
                    .build());

            Mother mother = motherRepository.findAll().get(0);
            createHelpRequest(mother, RequestStatus.PENDING, RequestType.EMERGENCY);

            List<ZoneStatsDto> zoneStats = dashboardService.getZoneStats();

            ZoneStatsDto zoneA = zoneStats.stream()
                    .filter(z -> z.zone().equals("ZONE-A"))
                    .findFirst().orElseThrow();

            assertThat(zoneA.motherCount()).isEqualTo(2);
            assertThat(zoneA.volunteerCount()).isEqualTo(1);
            assertThat(zoneA.availableVolunteers()).isEqualTo(1);
            assertThat(zoneA.pendingEmergencies()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return empty list when no zones exist")
        void testGetZoneStats_NoZones() {
            List<ZoneStatsDto> zoneStats = dashboardService.getZoneStats();

            assertThat(zoneStats).isEmpty();
        }
    }

    // Helper methods
    private Mother createMother(String zone, RiskLevel riskLevel) {
        return motherRepository.save(Mother.builder()
                .phoneNumber(generatePhoneNumber())
                .name("Test Mother")
                .camp("CAMP-A")
                .zone(zone)
                .riskLevel(riskLevel)
                .preferredLanguage(Language.ARABIC)
                .registeredAt(LocalDateTime.now())
                .build());
    }

    private Mother createMotherWithDueDate(String zone, LocalDate dueDate) {
        return motherRepository.save(Mother.builder()
                .phoneNumber(generatePhoneNumber())
                .name("Test Mother")
                .camp("CAMP-A")
                .zone(zone)
                .dueDate(dueDate)
                .riskLevel(RiskLevel.LOW)
                .preferredLanguage(Language.ARABIC)
                .registeredAt(LocalDateTime.now())
                .build());
    }

    private Volunteer createVolunteer(SkillType skillType, AvailabilityStatus availability) {
        return volunteerRepository.save(Volunteer.builder()
                .phoneNumber(generatePhoneNumber())
                .name("Test Volunteer")
                .camp("CAMP-A")
                .skillType(skillType)
                .zones(Set.of("ZONE-A"))
                .availability(availability)
                .preferredLanguage(Language.ARABIC)
                .registeredAt(LocalDateTime.now())
                .build());
    }

    private int caseCounter = 0;
    
    private HelpRequest createHelpRequest(Mother mother, RequestStatus status, RequestType type) {
        caseCounter++;
        return helpRequestRepository.save(HelpRequest.builder()
                .caseId(String.format("HR-%04d", caseCounter + 1000))
                .mother(mother)
                .requestType(type)
                .status(status)
                .zone(mother.getZone())
                .riskLevel(mother.getRiskLevel())
                .createdAt(LocalDateTime.now())
                .build());
    }
}
