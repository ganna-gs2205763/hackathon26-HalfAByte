package com.safebirth.domain.mother;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MotherService.
 */
@ExtendWith(MockitoExtension.class)
class MotherServiceTest {

    @Mock
    private MotherRepository motherRepository;

    @InjectMocks
    private MotherService motherService;

    private static final String TEST_PHONE = "+1234567890";
    private static final String TEST_CAMP = "Camp A";
    private static final String TEST_ZONE = "3";

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Register new mother with minimal fields")
        void testRegisterNewMother_Minimal() {
            // Arrange
            when(motherRepository.findByPhoneNumber(TEST_PHONE)).thenReturn(Optional.empty());
            when(motherRepository.save(any(Mother.class))).thenAnswer(invocation -> {
                Mother mother = invocation.getArgument(0);
                mother.setId(1L);
                return mother;
            });

            // Act
            Mother result = motherService.register(TEST_PHONE, TEST_CAMP, TEST_ZONE, Language.ENGLISH);

            // Assert
            assertNotNull(result);
            assertEquals(TEST_PHONE, result.getPhoneNumber());
            assertEquals(TEST_CAMP, result.getCamp());
            assertEquals(TEST_ZONE, result.getZone());
            assertEquals(Language.ENGLISH, result.getPreferredLanguage());
            assertEquals(RiskLevel.LOW, result.getRiskLevel());
            verify(motherRepository).save(any(Mother.class));
        }

        @Test
        @DisplayName("Register new mother with all fields")
        void testRegisterNewMother_AllFields() {
            // Arrange
            LocalDate dueDate = LocalDate.of(2026, 6, 15);
            when(motherRepository.findByPhoneNumber(TEST_PHONE)).thenReturn(Optional.empty());
            when(motherRepository.save(any(Mother.class))).thenAnswer(invocation -> {
                Mother mother = invocation.getArgument(0);
                mother.setId(1L);
                return mother;
            });

            // Act
            Mother result = motherService.register(TEST_PHONE, TEST_CAMP, TEST_ZONE, 
                    dueDate, RiskLevel.HIGH, Language.ARABIC);

            // Assert
            assertNotNull(result);
            assertEquals(TEST_PHONE, result.getPhoneNumber());
            assertEquals(dueDate, result.getDueDate());
            assertEquals(RiskLevel.HIGH, result.getRiskLevel());
            assertEquals(Language.ARABIC, result.getPreferredLanguage());
        }

        @Test
        @DisplayName("Update existing mother registration")
        void testUpdateExistingMother() {
            // Arrange
            Mother existingMother = Mother.builder()
                    .id(1L)
                    .phoneNumber(TEST_PHONE)
                    .camp("Old Camp")
                    .zone("1")
                    .preferredLanguage(Language.ENGLISH)
                    .riskLevel(RiskLevel.LOW)
                    .registeredAt(LocalDateTime.now().minusDays(7))
                    .build();

            when(motherRepository.findByPhoneNumber(TEST_PHONE)).thenReturn(Optional.of(existingMother));
            when(motherRepository.save(any(Mother.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Mother result = motherService.register(TEST_PHONE, TEST_CAMP, TEST_ZONE, Language.ARABIC);

            // Assert
            assertEquals(TEST_CAMP, result.getCamp());
            assertEquals(TEST_ZONE, result.getZone());
            assertEquals(Language.ARABIC, result.getPreferredLanguage());
            assertNotNull(result.getLastContactAt());
        }

        @Test
        @DisplayName("Update existing mother with new due date and risk level")
        void testUpdateExistingMother_WithDueDateAndRisk() {
            // Arrange
            Mother existingMother = Mother.builder()
                    .id(1L)
                    .phoneNumber(TEST_PHONE)
                    .camp("Old Camp")
                    .zone("1")
                    .riskLevel(RiskLevel.LOW)
                    .build();

            LocalDate newDueDate = LocalDate.of(2026, 8, 1);
            when(motherRepository.findByPhoneNumber(TEST_PHONE)).thenReturn(Optional.of(existingMother));
            when(motherRepository.save(any(Mother.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Mother result = motherService.register(TEST_PHONE, TEST_CAMP, TEST_ZONE,
                    newDueDate, RiskLevel.HIGH, Language.ARABIC);

            // Assert
            assertEquals(newDueDate, result.getDueDate());
            assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        }
    }

    @Nested
    @DisplayName("Find Operations Tests")
    class FindOperationsTests {

        @Test
        @DisplayName("Find mother by phone number - found")
        void testFindByPhone_Found() {
            // Arrange
            Mother mother = Mother.builder()
                    .id(1L)
                    .phoneNumber(TEST_PHONE)
                    .camp(TEST_CAMP)
                    .zone(TEST_ZONE)
                    .build();

            when(motherRepository.findByPhoneNumber(TEST_PHONE)).thenReturn(Optional.of(mother));

            // Act
            Optional<Mother> result = motherService.findByPhone(TEST_PHONE);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(TEST_PHONE, result.get().getPhoneNumber());
        }

        @Test
        @DisplayName("Find mother by phone number - not found")
        void testFindByPhone_NotFound() {
            // Arrange
            when(motherRepository.findByPhoneNumber(anyString())).thenReturn(Optional.empty());

            // Act
            Optional<Mother> result = motherService.findByPhone(TEST_PHONE);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Find mother by ID")
        void testFindById() {
            // Arrange
            Mother mother = Mother.builder().id(1L).build();
            when(motherRepository.findById(1L)).thenReturn(Optional.of(mother));

            // Act
            Optional<Mother> result = motherService.findById(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
        }

        @Test
        @DisplayName("Find mothers by zone")
        void testFindByZone() {
            // Arrange
            List<Mother> mothers = List.of(
                    Mother.builder().id(1L).zone(TEST_ZONE).build(),
                    Mother.builder().id(2L).zone(TEST_ZONE).build()
            );
            when(motherRepository.findByZone(TEST_ZONE)).thenReturn(mothers);

            // Act
            List<Mother> result = motherService.findByZone(TEST_ZONE);

            // Assert
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Find all mothers")
        void testFindAll() {
            // Arrange
            List<Mother> mothers = List.of(
                    Mother.builder().id(1L).build(),
                    Mother.builder().id(2L).build(),
                    Mother.builder().id(3L).build()
            );
            when(motherRepository.findAll()).thenReturn(mothers);

            // Act
            List<Mother> result = motherService.findAll();

            // Assert
            assertEquals(3, result.size());
        }
    }

    @Nested
    @DisplayName("Update Operations Tests")
    class UpdateOperationsTests {

        @Test
        @DisplayName("Update risk level")
        void testUpdateRiskLevel() {
            // Arrange
            Mother mother = Mother.builder()
                    .id(1L)
                    .phoneNumber(TEST_PHONE)
                    .riskLevel(RiskLevel.LOW)
                    .build();

            when(motherRepository.findById(1L)).thenReturn(Optional.of(mother));
            when(motherRepository.save(any(Mother.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Mother result = motherService.updateRiskLevel(1L, RiskLevel.HIGH);

            // Assert
            assertEquals(RiskLevel.HIGH, result.getRiskLevel());
            verify(motherRepository).save(mother);
        }

        @Test
        @DisplayName("Update risk level - mother not found")
        void testUpdateRiskLevel_NotFound() {
            // Arrange
            when(motherRepository.findById(anyLong())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, 
                    () -> motherService.updateRiskLevel(999L, RiskLevel.HIGH));
        }

        @Test
        @DisplayName("Update due date")
        void testUpdateDueDate() {
            // Arrange
            Mother mother = Mother.builder()
                    .id(1L)
                    .phoneNumber(TEST_PHONE)
                    .build();

            LocalDate newDueDate = LocalDate.of(2026, 7, 20);
            when(motherRepository.findById(1L)).thenReturn(Optional.of(mother));
            when(motherRepository.save(any(Mother.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Mother result = motherService.updateDueDate(1L, newDueDate);

            // Assert
            assertEquals(newDueDate, result.getDueDate());
        }

        @Test
        @DisplayName("Update due date - mother not found")
        void testUpdateDueDate_NotFound() {
            // Arrange
            when(motherRepository.findById(anyLong())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, 
                    () -> motherService.updateDueDate(999L, LocalDate.now()));
        }

        @Test
        @DisplayName("Record contact updates lastContactAt")
        void testRecordContact() {
            // Arrange
            Mother mother = Mother.builder()
                    .id(1L)
                    .phoneNumber(TEST_PHONE)
                    .build();

            when(motherRepository.findByPhoneNumber(TEST_PHONE)).thenReturn(Optional.of(mother));
            when(motherRepository.save(any(Mother.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            motherService.recordContact(TEST_PHONE);

            // Assert
            verify(motherRepository).save(mother);
            assertNotNull(mother.getLastContactAt());
        }
    }

    @Nested
    @DisplayName("Count Operations Tests")
    class CountOperationsTests {

        @Test
        @DisplayName("Count mothers by zone")
        void testCountByZone() {
            // Arrange
            when(motherRepository.countByZone(TEST_ZONE)).thenReturn(5L);

            // Act
            long count = motherService.countByZone(TEST_ZONE);

            // Assert
            assertEquals(5L, count);
        }
    }

    @Nested
    @DisplayName("Zone Operations Tests")
    class ZoneOperationsTests {

        @Test
        @DisplayName("Get all zones")
        void testGetAllZones() {
            // Arrange
            List<String> zones = List.of("1", "2", "3", "4");
            when(motherRepository.findAllZones()).thenReturn(zones);

            // Act
            List<String> result = motherService.getAllZones();

            // Assert
            assertEquals(4, result.size());
            assertTrue(result.contains("3"));
        }
    }
}
