package com.safebirth.domain.helprequest;

import com.safebirth.domain.mother.Mother;
import com.safebirth.domain.mother.RiskLevel;
import com.safebirth.domain.volunteer.Volunteer;
import com.safebirth.domain.volunteer.SkillType;
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
 * Unit tests for HelpRequestService.
 */
@ExtendWith(MockitoExtension.class)
class HelpRequestServiceTest {

    @Mock
    private HelpRequestRepository helpRequestRepository;

    @InjectMocks
    private HelpRequestService helpRequestService;

    private Mother createTestMother() {
        return Mother.builder()
                .id(1L)
                .phoneNumber("+1234567890")
                .camp("Camp A")
                .zone("3")
                .riskLevel(RiskLevel.HIGH)
                .dueDate(LocalDate.of(2026, 6, 15))
                .build();
    }

    private Volunteer createTestVolunteer() {
        return Volunteer.builder()
                .id(1L)
                .phoneNumber("+0987654321")
                .name("Fatima")
                .skillType(SkillType.MIDWIFE)
                .build();
    }

    @Nested
    @DisplayName("Create Request Tests")
    class CreateRequestTests {

        @Test
        @DisplayName("Create emergency help request")
        void testCreateEmergencyRequest() {
            // Arrange
            Mother mother = createTestMother();
            when(helpRequestRepository.findMaxCaseNumber()).thenReturn(null);
            when(helpRequestRepository.save(any(HelpRequest.class))).thenAnswer(invocation -> {
                HelpRequest request = invocation.getArgument(0);
                request.setId(1L);
                return request;
            });

            // Act
            HelpRequest result = helpRequestService.createRequest(mother, RequestType.EMERGENCY);

            // Assert
            assertNotNull(result);
            assertEquals("HR-0001", result.getCaseId());
            assertEquals(mother, result.getMother());
            assertEquals(RequestType.EMERGENCY, result.getRequestType());
            assertEquals(RequestStatus.PENDING, result.getStatus());
            assertEquals(mother.getZone(), result.getZone());
            assertEquals(mother.getRiskLevel(), result.getRiskLevel());
            assertEquals(mother.getDueDate(), result.getDueDate());
            assertTrue(result.isEmergency());
            assertTrue(result.isActive());
            verify(helpRequestRepository).save(any(HelpRequest.class));
        }

        @Test
        @DisplayName("Create support help request")
        void testCreateSupportRequest() {
            // Arrange
            Mother mother = createTestMother();
            when(helpRequestRepository.findMaxCaseNumber()).thenReturn(null);
            when(helpRequestRepository.save(any(HelpRequest.class))).thenAnswer(invocation -> {
                HelpRequest request = invocation.getArgument(0);
                request.setId(1L);
                return request;
            });

            // Act
            HelpRequest result = helpRequestService.createRequest(mother, RequestType.SUPPORT);

            // Assert
            assertNotNull(result);
            assertEquals(RequestType.SUPPORT, result.getRequestType());
            assertFalse(result.isEmergency());
        }

        @Test
        @DisplayName("Generate sequential case IDs")
        void testGenerateSequentialCaseIds() {
            // Arrange
            Mother mother = createTestMother();
            when(helpRequestRepository.findMaxCaseNumber()).thenReturn(41);
            when(helpRequestRepository.save(any(HelpRequest.class))).thenAnswer(invocation -> {
                HelpRequest request = invocation.getArgument(0);
                request.setId(1L);
                return request;
            });

            // Act
            HelpRequest result = helpRequestService.createRequest(mother, RequestType.EMERGENCY);

            // Assert
            assertEquals("HR-0042", result.getCaseId());
        }
    }

    @Nested
    @DisplayName("Find Request Tests")
    class FindRequestTests {

        @Test
        @DisplayName("Find request by case ID with HR prefix")
        void testFindByCaseId_WithPrefix() {
            // Arrange
            HelpRequest request = HelpRequest.builder()
                    .id(1L)
                    .caseId("HR-0042")
                    .build();
            when(helpRequestRepository.findByCaseId("HR-0042")).thenReturn(Optional.of(request));

            // Act
            Optional<HelpRequest> result = helpRequestService.findByCaseId("HR-0042");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("HR-0042", result.get().getCaseId());
        }

        @Test
        @DisplayName("Find request by case ID without HR prefix")
        void testFindByCaseId_WithoutPrefix() {
            // Arrange
            HelpRequest request = HelpRequest.builder()
                    .id(1L)
                    .caseId("HR-0042")
                    .build();
            when(helpRequestRepository.findByCaseId("HR-0042")).thenReturn(Optional.of(request));

            // Act
            Optional<HelpRequest> result = helpRequestService.findByCaseId("0042");

            // Assert
            assertTrue(result.isPresent());
        }

        @Test
        @DisplayName("Find request by case ID - not found")
        void testFindByCaseId_NotFound() {
            // Arrange
            when(helpRequestRepository.findByCaseId(anyString())).thenReturn(Optional.empty());

            // Act
            Optional<HelpRequest> result = helpRequestService.findByCaseId("HR-9999");

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Accept Request Tests")
    class AcceptRequestTests {

        @Test
        @DisplayName("Accept pending request")
        void testAcceptRequest() {
            // Arrange
            Mother mother = createTestMother();
            Volunteer volunteer = createTestVolunteer();
            HelpRequest request = HelpRequest.builder()
                    .id(1L)
                    .caseId("HR-0042")
                    .mother(mother)
                    .requestType(RequestType.EMERGENCY)
                    .status(RequestStatus.PENDING)
                    .zone(mother.getZone())
                    .build();

            when(helpRequestRepository.findByCaseId("HR-0042")).thenReturn(Optional.of(request));
            when(helpRequestRepository.save(any(HelpRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            HelpRequest result = helpRequestService.acceptRequest("HR-0042", volunteer);

            // Assert
            assertEquals(RequestStatus.ACCEPTED, result.getStatus());
            assertEquals(volunteer, result.getAcceptedBy());
            assertNotNull(result.getAcceptedAt());
            assertTrue(result.isActive());
        }

        @Test
        @DisplayName("Accept request - not found")
        void testAcceptRequest_NotFound() {
            // Arrange
            Volunteer volunteer = createTestVolunteer();
            when(helpRequestRepository.findByCaseId(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> helpRequestService.acceptRequest("HR-9999", volunteer));
        }

        @Test
        @DisplayName("Accept request - already accepted")
        void testAcceptRequest_AlreadyAccepted() {
            // Arrange
            Volunteer volunteer = createTestVolunteer();
            HelpRequest request = HelpRequest.builder()
                    .id(1L)
                    .caseId("HR-0042")
                    .status(RequestStatus.ACCEPTED)
                    .build();

            when(helpRequestRepository.findByCaseId("HR-0042")).thenReturn(Optional.of(request));

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> helpRequestService.acceptRequest("HR-0042", volunteer));
        }

        @Test
        @DisplayName("Accept request - already completed")
        void testAcceptRequest_AlreadyCompleted() {
            // Arrange
            Volunteer volunteer = createTestVolunteer();
            HelpRequest request = HelpRequest.builder()
                    .id(1L)
                    .caseId("HR-0042")
                    .status(RequestStatus.COMPLETED)
                    .build();

            when(helpRequestRepository.findByCaseId("HR-0042")).thenReturn(Optional.of(request));

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> helpRequestService.acceptRequest("HR-0042", volunteer));
        }
    }

    @Nested
    @DisplayName("Start Progress Tests")
    class StartProgressTests {

        @Test
        @DisplayName("Start progress on accepted request")
        void testStartProgress() {
            // Arrange
            HelpRequest request = HelpRequest.builder()
                    .id(1L)
                    .caseId("HR-0042")
                    .status(RequestStatus.ACCEPTED)
                    .build();

            when(helpRequestRepository.findByCaseId("HR-0042")).thenReturn(Optional.of(request));
            when(helpRequestRepository.save(any(HelpRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            HelpRequest result = helpRequestService.startProgress("HR-0042");

            // Assert
            assertEquals(RequestStatus.IN_PROGRESS, result.getStatus());
            assertNotNull(result.getInProgressAt());
        }

        @Test
        @DisplayName("Start progress - not accepted")
        void testStartProgress_NotAccepted() {
            // Arrange
            HelpRequest request = HelpRequest.builder()
                    .id(1L)
                    .caseId("HR-0042")
                    .status(RequestStatus.PENDING)
                    .build();

            when(helpRequestRepository.findByCaseId("HR-0042")).thenReturn(Optional.of(request));

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> helpRequestService.startProgress("HR-0042"));
        }
    }

    @Nested
    @DisplayName("Complete Request Tests")
    class CompleteRequestTests {

        @Test
        @DisplayName("Complete accepted request")
        void testCompleteRequest_Accepted() {
            // Arrange
            HelpRequest request = HelpRequest.builder()
                    .id(1L)
                    .caseId("HR-0042")
                    .status(RequestStatus.ACCEPTED)
                    .build();

            when(helpRequestRepository.findByCaseId("HR-0042")).thenReturn(Optional.of(request));
            when(helpRequestRepository.save(any(HelpRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            HelpRequest result = helpRequestService.completeRequest("HR-0042");

            // Assert
            assertEquals(RequestStatus.COMPLETED, result.getStatus());
            assertNotNull(result.getClosedAt());
            assertFalse(result.isActive());
        }

        @Test
        @DisplayName("Complete in-progress request")
        void testCompleteRequest_InProgress() {
            // Arrange
            HelpRequest request = HelpRequest.builder()
                    .id(1L)
                    .caseId("HR-0042")
                    .status(RequestStatus.IN_PROGRESS)
                    .build();

            when(helpRequestRepository.findByCaseId("HR-0042")).thenReturn(Optional.of(request));
            when(helpRequestRepository.save(any(HelpRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            HelpRequest result = helpRequestService.completeRequest("HR-0042");

            // Assert
            assertEquals(RequestStatus.COMPLETED, result.getStatus());
        }

        @Test
        @DisplayName("Complete request - already completed")
        void testCompleteRequest_AlreadyCompleted() {
            // Arrange
            HelpRequest request = HelpRequest.builder()
                    .id(1L)
                    .caseId("HR-0042")
                    .status(RequestStatus.COMPLETED)
                    .closedAt(LocalDateTime.now())
                    .build();

            when(helpRequestRepository.findByCaseId("HR-0042")).thenReturn(Optional.of(request));

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> helpRequestService.completeRequest("HR-0042"));
        }
    }

    @Nested
    @DisplayName("Cancel Request Tests")
    class CancelRequestTests {

        @Test
        @DisplayName("Cancel pending request")
        void testCancelRequest_Pending() {
            // Arrange
            HelpRequest request = HelpRequest.builder()
                    .id(1L)
                    .caseId("HR-0042")
                    .status(RequestStatus.PENDING)
                    .build();

            when(helpRequestRepository.findByCaseId("HR-0042")).thenReturn(Optional.of(request));
            when(helpRequestRepository.save(any(HelpRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            HelpRequest result = helpRequestService.cancelRequest("HR-0042");

            // Assert
            assertEquals(RequestStatus.CANCELLED, result.getStatus());
            assertNotNull(result.getClosedAt());
            assertFalse(result.isActive());
        }

        @Test
        @DisplayName("Cancel accepted request")
        void testCancelRequest_Accepted() {
            // Arrange
            HelpRequest request = HelpRequest.builder()
                    .id(1L)
                    .caseId("HR-0042")
                    .status(RequestStatus.ACCEPTED)
                    .build();

            when(helpRequestRepository.findByCaseId("HR-0042")).thenReturn(Optional.of(request));
            when(helpRequestRepository.save(any(HelpRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            HelpRequest result = helpRequestService.cancelRequest("HR-0042");

            // Assert
            assertEquals(RequestStatus.CANCELLED, result.getStatus());
        }

        @Test
        @DisplayName("Cancel request - already cancelled")
        void testCancelRequest_AlreadyCancelled() {
            // Arrange
            HelpRequest request = HelpRequest.builder()
                    .id(1L)
                    .caseId("HR-0042")
                    .status(RequestStatus.CANCELLED)
                    .closedAt(LocalDateTime.now())
                    .build();

            when(helpRequestRepository.findByCaseId("HR-0042")).thenReturn(Optional.of(request));

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> helpRequestService.cancelRequest("HR-0042"));
        }
    }

    @Nested
    @DisplayName("Query Operations Tests")
    class QueryOperationsTests {

        @Test
        @DisplayName("Find pending requests in zone")
        void testFindPendingInZone() {
            // Arrange
            List<HelpRequest> requests = List.of(
                    HelpRequest.builder().id(1L).zone("3").status(RequestStatus.PENDING).build(),
                    HelpRequest.builder().id(2L).zone("3").status(RequestStatus.PENDING).build()
            );
            when(helpRequestRepository.findByZoneAndStatus("3", RequestStatus.PENDING)).thenReturn(requests);

            // Act
            List<HelpRequest> result = helpRequestService.findPendingInZone("3");

            // Assert
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Find active requests by volunteer")
        void testFindActiveByVolunteer() {
            // Arrange
            List<HelpRequest> requests = List.of(
                    HelpRequest.builder().id(1L).status(RequestStatus.ACCEPTED).build(),
                    HelpRequest.builder().id(2L).status(RequestStatus.IN_PROGRESS).build()
            );
            when(helpRequestRepository.findActiveByVolunteer(1L)).thenReturn(requests);

            // Act
            List<HelpRequest> result = helpRequestService.findActiveByVolunteer(1L);

            // Assert
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Find recent requests")
        void testFindRecent() {
            // Arrange
            List<HelpRequest> requests = List.of(
                    HelpRequest.builder().id(1L).build(),
                    HelpRequest.builder().id(2L).build()
            );
            when(helpRequestRepository.findTop20ByOrderByCreatedAtDesc()).thenReturn(requests);

            // Act
            List<HelpRequest> result = helpRequestService.findRecent();

            // Assert
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Count pending emergencies")
        void testCountPendingEmergencies() {
            // Arrange
            when(helpRequestRepository.countPendingEmergencies()).thenReturn(5L);

            // Act
            long count = helpRequestService.countPendingEmergencies();

            // Assert
            assertEquals(5L, count);
        }

        @Test
        @DisplayName("Find all requests")
        void testFindAll() {
            // Arrange
            List<HelpRequest> requests = List.of(
                    HelpRequest.builder().id(1L).build(),
                    HelpRequest.builder().id(2L).build(),
                    HelpRequest.builder().id(3L).build()
            );
            when(helpRequestRepository.findAll()).thenReturn(requests);

            // Act
            List<HelpRequest> result = helpRequestService.findAll();

            // Assert
            assertEquals(3, result.size());
        }
    }

    @Nested
    @DisplayName("Alerts Counter Tests")
    class AlertsCounterTests {

        @Test
        @DisplayName("Increment alerts sent counter")
        void testIncrementAlertsSent() {
            // Arrange
            HelpRequest request = HelpRequest.builder()
                    .id(1L)
                    .caseId("HR-0042")
                    .alertsSent(3)
                    .build();

            when(helpRequestRepository.findByCaseId("HR-0042")).thenReturn(Optional.of(request));
            when(helpRequestRepository.save(any(HelpRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            helpRequestService.incrementAlertsSent("HR-0042");

            // Assert
            assertEquals(4, request.getAlertsSent());
            verify(helpRequestRepository).save(request);
        }

        @Test
        @DisplayName("Increment alerts sent - request not found")
        void testIncrementAlertsSent_NotFound() {
            // Arrange
            when(helpRequestRepository.findByCaseId(anyString())).thenReturn(Optional.empty());

            // Act - should not throw, just do nothing
            helpRequestService.incrementAlertsSent("HR-9999");

            // Assert
            verify(helpRequestRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("HelpRequest Entity Tests")
    class HelpRequestEntityTests {

        @Test
        @DisplayName("Test isActive for different statuses")
        void testIsActive() {
            // Assert
            assertTrue(HelpRequest.builder().status(RequestStatus.PENDING).build().isActive());
            assertTrue(HelpRequest.builder().status(RequestStatus.ACCEPTED).build().isActive());
            assertTrue(HelpRequest.builder().status(RequestStatus.IN_PROGRESS).build().isActive());
            assertFalse(HelpRequest.builder().status(RequestStatus.COMPLETED).build().isActive());
            assertFalse(HelpRequest.builder().status(RequestStatus.CANCELLED).build().isActive());
        }

        @Test
        @DisplayName("Test isEmergency")
        void testIsEmergency() {
            // Assert
            assertTrue(HelpRequest.builder().requestType(RequestType.EMERGENCY).build().isEmergency());
            assertFalse(HelpRequest.builder().requestType(RequestType.SUPPORT).build().isEmergency());
        }

        @Test
        @DisplayName("Test accept method")
        void testAcceptMethod() {
            // Arrange
            Volunteer volunteer = createTestVolunteer();
            HelpRequest request = HelpRequest.builder()
                    .id(1L)
                    .status(RequestStatus.PENDING)
                    .build();

            // Act
            request.accept(volunteer);

            // Assert
            assertEquals(RequestStatus.ACCEPTED, request.getStatus());
            assertEquals(volunteer, request.getAcceptedBy());
            assertNotNull(request.getAcceptedAt());
        }

        @Test
        @DisplayName("Test startProgress method")
        void testStartProgressMethod() {
            // Arrange
            HelpRequest request = HelpRequest.builder()
                    .id(1L)
                    .status(RequestStatus.ACCEPTED)
                    .build();

            // Act
            request.startProgress();

            // Assert
            assertEquals(RequestStatus.IN_PROGRESS, request.getStatus());
            assertNotNull(request.getInProgressAt());
        }

        @Test
        @DisplayName("Test complete method")
        void testCompleteMethod() {
            // Arrange
            HelpRequest request = HelpRequest.builder()
                    .id(1L)
                    .status(RequestStatus.IN_PROGRESS)
                    .build();

            // Act
            request.complete();

            // Assert
            assertEquals(RequestStatus.COMPLETED, request.getStatus());
            assertNotNull(request.getClosedAt());
        }

        @Test
        @DisplayName("Test cancel method")
        void testCancelMethod() {
            // Arrange
            HelpRequest request = HelpRequest.builder()
                    .id(1L)
                    .status(RequestStatus.PENDING)
                    .build();

            // Act
            request.cancel();

            // Assert
            assertEquals(RequestStatus.CANCELLED, request.getStatus());
            assertNotNull(request.getClosedAt());
        }
    }
}
