package com.safebirth.domain.helprequest;

import com.safebirth.domain.mother.Mother;
import com.safebirth.domain.mother.RiskLevel;
import com.safebirth.domain.volunteer.Volunteer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a help request from a mother.
 * Tracks the lifecycle from creation to resolution.
 */
@Entity
@Table(name = "help_requests", indexes = {
        @Index(name = "idx_hr_case_id", columnList = "caseId", unique = true),
        @Index(name = "idx_hr_status", columnList = "status"),
        @Index(name = "idx_hr_zone", columnList = "zone"),
        @Index(name = "idx_hr_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-readable case ID (HR-0001 format).
     */
    @Column(nullable = false, unique = true, length = 20)
    private String caseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mother_id", nullable = false)
    private Mother mother;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id")
    private Volunteer acceptedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    /**
     * Copied from mother at creation time for quick filtering.
     */
    @Column(nullable = false, length = 20)
    private String zone;

    /**
     * Copied from mother at creation time.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private RiskLevel riskLevel;

    /**
     * Copied from mother at creation time.
     */
    @Column
    private LocalDate dueDate;

    /**
     * Optional notes from the mother or volunteer.
     */
    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime acceptedAt;

    @Column
    private LocalDateTime inProgressAt;

    @Column
    private LocalDateTime closedAt;

    /**
     * Number of volunteer alerts sent for this request.
     */
    @Column
    @Builder.Default
    private int alertsSent = 0;

    /**
     * Check if this request is still active (can be accepted/worked on).
     *
     * @return true if the request is active
     */
    public boolean isActive() {
        return status == RequestStatus.PENDING 
                || status == RequestStatus.ACCEPTED 
                || status == RequestStatus.IN_PROGRESS;
    }

    /**
     * Check if this request is an emergency.
     *
     * @return true if emergency type
     */
    public boolean isEmergency() {
        return requestType == RequestType.EMERGENCY;
    }

    /**
     * Accept this request by a volunteer.
     *
     * @param volunteer the accepting volunteer
     */
    public void accept(Volunteer volunteer) {
        this.acceptedBy = volunteer;
        this.status = RequestStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    /**
     * Mark this request as in progress.
     */
    public void startProgress() {
        this.status = RequestStatus.IN_PROGRESS;
        this.inProgressAt = LocalDateTime.now();
    }

    /**
     * Complete this request.
     */
    public void complete() {
        this.status = RequestStatus.COMPLETED;
        this.closedAt = LocalDateTime.now();
    }

    /**
     * Cancel this request.
     */
    public void cancel() {
        this.status = RequestStatus.CANCELLED;
        this.closedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
