package com.safebirth.domain.volunteer;

import com.safebirth.domain.mother.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VolunteerService.
 */
@ExtendWith(MockitoExtension.class)
class VolunteerServiceTest {

    @Mock
    private VolunteerRepository volunteerRepository;

    @InjectMocks
    private VolunteerService volunteerService;

    private static final String TEST_PHONE = "+1234567890";
    private static final String TEST_NAME = "Fatima";
    private static final String TEST_CAMP = "Camp A";

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Register new volunteer with all fields")
        void testRegisterNewVolunteer() {
            // Arrange
            Set<String> zones = new HashSet<>(Set.of("3", "4", "5"));
            when(volunteerRepository.findByPhoneNumber(TEST_PHONE)).thenReturn(Optional.empty());
            when(volunteerRepository.save(any(Volunteer.class))).thenAnswer(invocation -> {
                Volunteer volunteer = invocation.getArgument(0);
                volunteer.setId(1L);
                return volunteer;
            });

            // Act
            Volunteer result = volunteerService.register(TEST_PHONE, TEST_NAME, TEST_CAMP, 
                    SkillType.MIDWIFE, zones, Language.ARABIC);

            // Assert
            assertNotNull(result);
            assertEquals(TEST_PHONE, result.getPhoneNumber());
            assertEquals(TEST_NAME, result.getName());
            assertEquals(TEST_CAMP, result.getCamp());
            assertEquals(SkillType.MIDWIFE, result.getSkillType());
            assertEquals(3, result.getZones().size());
            assertEquals(Language.ARABIC, result.getPreferredLanguage());
            assertEquals(AvailabilityStatus.AVAILABLE, result.getAvailability());
            verify(volunteerRepository).save(any(Volunteer.class));
        }

        @Test
        @DisplayName("Register new volunteer with single zone")
        void testRegisterNewVolunteer_SingleZone() {
            // Arrange
            Set<String> zones = new HashSet<>(Set.of("1"));
            when(volunteerRepository.findByPhoneNumber(TEST_PHONE)).thenReturn(Optional.empty());
            when(volunteerRepository.save(any(Volunteer.class))).thenAnswer(invocation -> {
                Volunteer volunteer = invocation.getArgument(0);
                volunteer.setId(1L);
                return volunteer;
            });

            // Act
            Volunteer result = volunteerService.register(TEST_PHONE, TEST_NAME, TEST_CAMP,
                    SkillType.NURSE, zones, Language.ENGLISH);

            // Assert
            assertEquals(1, result.getZones().size());
            assertTrue(result.getZones().contains("1"));
        }

        @Test
        @DisplayName("Update existing volunteer registration")
        void testUpdateExistingVolunteer() {
            // Arrange
            Volunteer existingVolunteer = Volunteer.builder()
                    .id(1L)
                    .phoneNumber(TEST_PHONE)
                    .name("Old Name")
                    .camp("Old Camp")
                    .skillType(SkillType.COMMUNITY_VOLUNTEER)
                    .zones(new HashSet<>(Set.of("1")))
                    .preferredLanguage(Language.ENGLISH)
                    .registeredAt(LocalDateTime.now().minusDays(7))
                    .build();

            Set<String> newZones = new HashSet<>(Set.of("3", "4", "5"));
            when(volunteerRepository.findByPhoneNumber(TEST_PHONE)).thenReturn(Optional.of(existingVolunteer));
            when(volunteerRepository.save(any(Volunteer.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Volunteer result = volunteerService.register(TEST_PHONE, TEST_NAME, TEST_CAMP,
                    SkillType.MIDWIFE, newZones, Language.ARABIC);

            // Assert
            assertEquals(TEST_NAME, result.getName());
            assertEquals(TEST_CAMP, result.getCamp());
            assertEquals(SkillType.MIDWIFE, result.getSkillType());
            assertEquals(3, result.getZones().size());
            assertEquals(Language.ARABIC, result.getPreferredLanguage());
            assertNotNull(result.getLastActiveAt());
        }
    }

    @Nested
    @DisplayName("Find Operations Tests")
    class FindOperationsTests {

        @Test
        @DisplayName("Find volunteer by phone number - found")
        void testFindByPhone_Found() {
            // Arrange
            Volunteer volunteer = Volunteer.builder()
                    .id(1L)
                    .phoneNumber(TEST_PHONE)
                    .name(TEST_NAME)
                    .build();

            when(volunteerRepository.findByPhoneNumber(TEST_PHONE)).thenReturn(Optional.of(volunteer));

            // Act
            Optional<Volunteer> result = volunteerService.findByPhone(TEST_PHONE);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(TEST_PHONE, result.get().getPhoneNumber());
        }

        @Test
        @DisplayName("Find volunteer by phone number - not found")
        void testFindByPhone_NotFound() {
            // Arrange
            when(volunteerRepository.findByPhoneNumber(anyString())).thenReturn(Optional.empty());

            // Act
            Optional<Volunteer> result = volunteerService.findByPhone(TEST_PHONE);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Find volunteer by ID")
        void testFindById() {
            // Arrange
            Volunteer volunteer = Volunteer.builder().id(1L).build();
            when(volunteerRepository.findById(1L)).thenReturn(Optional.of(volunteer));

            // Act
            Optional<Volunteer> result = volunteerService.findById(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
        }

        @Test
        @DisplayName("Find available volunteers for zone")
        void testFindAvailableForZone() {
            // Arrange
            List<Volunteer> volunteers = List.of(
                    Volunteer.builder().id(1L).skillType(SkillType.MIDWIFE).build(),
                    Volunteer.builder().id(2L).skillType(SkillType.NURSE).build()
            );
            when(volunteerRepository.findAvailableByZoneOrderedBySkill("3")).thenReturn(volunteers);

            // Act
            List<Volunteer> result = volunteerService.findAvailableForZone("3");

            // Assert
            assertEquals(2, result.size());
            // First should be higher priority (MIDWIFE)
            assertEquals(SkillType.MIDWIFE, result.get(0).getSkillType());
        }

        @Test
        @DisplayName("Find all available volunteers")
        void testFindAvailable() {
            // Arrange
            List<Volunteer> volunteers = List.of(
                    Volunteer.builder().id(1L).availability(AvailabilityStatus.AVAILABLE).build(),
                    Volunteer.builder().id(2L).availability(AvailabilityStatus.AVAILABLE).build()
            );
            when(volunteerRepository.findByAvailability(AvailabilityStatus.AVAILABLE)).thenReturn(volunteers);

            // Act
            List<Volunteer> result = volunteerService.findAvailable();

            // Assert
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Find all volunteers")
        void testFindAll() {
            // Arrange
            List<Volunteer> volunteers = List.of(
                    Volunteer.builder().id(1L).build(),
                    Volunteer.builder().id(2L).build(),
                    Volunteer.builder().id(3L).build()
            );
            when(volunteerRepository.findAll()).thenReturn(volunteers);

            // Act
            List<Volunteer> result = volunteerService.findAll();

            // Assert
            assertEquals(3, result.size());
        }
    }

    @Nested
    @DisplayName("Availability Update Tests")
    class AvailabilityUpdateTests {

        @Test
        @DisplayName("Update availability to AVAILABLE")
        void testUpdateAvailability_Available() {
            // Arrange
            Volunteer volunteer = Volunteer.builder()
                    .id(1L)
                    .phoneNumber(TEST_PHONE)
                    .availability(AvailabilityStatus.BUSY)
                    .build();

            when(volunteerRepository.findByPhoneNumber(TEST_PHONE)).thenReturn(Optional.of(volunteer));
            when(volunteerRepository.save(any(Volunteer.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Volunteer result = volunteerService.updateAvailability(TEST_PHONE, AvailabilityStatus.AVAILABLE);

            // Assert
            assertEquals(AvailabilityStatus.AVAILABLE, result.getAvailability());
            assertNotNull(result.getLastActiveAt());
            verify(volunteerRepository).save(volunteer);
        }

        @Test
        @DisplayName("Update availability to BUSY")
        void testUpdateAvailability_Busy() {
            // Arrange
            Volunteer volunteer = Volunteer.builder()
                    .id(1L)
                    .phoneNumber(TEST_PHONE)
                    .availability(AvailabilityStatus.AVAILABLE)
                    .build();

            when(volunteerRepository.findByPhoneNumber(TEST_PHONE)).thenReturn(Optional.of(volunteer));
            when(volunteerRepository.save(any(Volunteer.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Volunteer result = volunteerService.updateAvailability(TEST_PHONE, AvailabilityStatus.BUSY);

            // Assert
            assertEquals(AvailabilityStatus.BUSY, result.getAvailability());
        }

        @Test
        @DisplayName("Update availability to OFFLINE")
        void testUpdateAvailability_Offline() {
            // Arrange
            Volunteer volunteer = Volunteer.builder()
                    .id(1L)
                    .phoneNumber(TEST_PHONE)
                    .availability(AvailabilityStatus.AVAILABLE)
                    .build();

            when(volunteerRepository.findByPhoneNumber(TEST_PHONE)).thenReturn(Optional.of(volunteer));
            when(volunteerRepository.save(any(Volunteer.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Volunteer result = volunteerService.updateAvailability(TEST_PHONE, AvailabilityStatus.OFFLINE);

            // Assert
            assertEquals(AvailabilityStatus.OFFLINE, result.getAvailability());
        }

        @Test
        @DisplayName("Update availability - volunteer not found")
        void testUpdateAvailability_NotFound() {
            // Arrange
            when(volunteerRepository.findByPhoneNumber(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> volunteerService.updateAvailability(TEST_PHONE, AvailabilityStatus.AVAILABLE));
        }
    }

    @Nested
    @DisplayName("Completed Cases Tests")
    class CompletedCasesTests {

        @Test
        @DisplayName("Increment completed cases count")
        void testIncrementCompletedCases() {
            // Arrange
            Volunteer volunteer = Volunteer.builder()
                    .id(1L)
                    .phoneNumber(TEST_PHONE)
                    .completedCases(5)
                    .build();

            when(volunteerRepository.findById(1L)).thenReturn(Optional.of(volunteer));
            when(volunteerRepository.save(any(Volunteer.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            volunteerService.incrementCompletedCases(1L);

            // Assert
            assertEquals(6, volunteer.getCompletedCases());
            verify(volunteerRepository).save(volunteer);
        }

        @Test
        @DisplayName("Increment completed cases - volunteer not found")
        void testIncrementCompletedCases_NotFound() {
            // Arrange
            when(volunteerRepository.findById(anyLong())).thenReturn(Optional.empty());

            // Act - should not throw, just do nothing
            volunteerService.incrementCompletedCases(999L);

            // Assert
            verify(volunteerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Count Operations Tests")
    class CountOperationsTests {

        @Test
        @DisplayName("Count available volunteers")
        void testCountAvailable() {
            // Arrange
            when(volunteerRepository.countByAvailability(AvailabilityStatus.AVAILABLE)).thenReturn(10L);

            // Act
            long count = volunteerService.countAvailable();

            // Assert
            assertEquals(10L, count);
        }
    }

    @Nested
    @DisplayName("Volunteer Entity Tests")
    class VolunteerEntityTests {

        @Test
        @DisplayName("Test coversZone method")
        void testCoversZone() {
            // Arrange
            Volunteer volunteer = Volunteer.builder()
                    .zones(new HashSet<>(Set.of("3", "4", "5")))
                    .build();

            // Assert
            assertTrue(volunteer.coversZone("3"));
            assertTrue(volunteer.coversZone("4"));
            assertTrue(volunteer.coversZone("5"));
            assertFalse(volunteer.coversZone("1"));
            assertFalse(volunteer.coversZone("2"));
        }

        @Test
        @DisplayName("Test isAvailable method")
        void testIsAvailable() {
            // Arrange
            Volunteer availableVolunteer = Volunteer.builder()
                    .availability(AvailabilityStatus.AVAILABLE)
                    .build();
            Volunteer busyVolunteer = Volunteer.builder()
                    .availability(AvailabilityStatus.BUSY)
                    .build();
            Volunteer offlineVolunteer = Volunteer.builder()
                    .availability(AvailabilityStatus.OFFLINE)
                    .build();

            // Assert
            assertTrue(availableVolunteer.isAvailable());
            assertFalse(busyVolunteer.isAvailable());
            assertFalse(offlineVolunteer.isAvailable());
        }

        @Test
        @DisplayName("Test getFormattedId")
        void testGetFormattedId() {
            // Arrange
            Volunteer volunteer = Volunteer.builder().id(42L).build();

            // Act
            String formattedId = volunteer.getFormattedId();

            // Assert
            assertEquals("V-0042", formattedId);
        }
    }
}
