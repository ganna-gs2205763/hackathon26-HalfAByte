package com.safebirth.domain.helprequest;

import com.safebirth.domain.mother.Mother;
import com.safebirth.domain.mother.RiskLevel;
import com.safebirth.domain.volunteer.Volunteer;
import jakarta.persistence.*;

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
    private int alertsSent = 0;

    public HelpRequest() {
    }

    public HelpRequest(Long id, String caseId, Mother mother, Volunteer acceptedBy, RequestType requestType,
                       RequestStatus status, String zone, RiskLevel riskLevel, LocalDate dueDate, String notes,
                       LocalDateTime createdAt, LocalDateTime acceptedAt, LocalDateTime inProgressAt,
                       LocalDateTime closedAt, int alertsSent) {
        this.id = id;
        this.caseId = caseId;
        this.mother = mother;
        this.acceptedBy = acceptedBy;
        this.requestType = requestType;
        this.status = status != null ? status : RequestStatus.PENDING;
        this.zone = zone;
        this.riskLevel = riskLevel;
        this.dueDate = dueDate;
        this.notes = notes;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.acceptedAt = acceptedAt;
        this.inProgressAt = inProgressAt;
        this.closedAt = closedAt;
        this.alertsSent = alertsSent;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getCaseId() {
        return caseId;
    }

    public Mother getMother() {
        return mother;
    }

    public Volunteer getAcceptedBy() {
        return acceptedBy;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public String getZone() {
        return zone;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public LocalDateTime getInProgressAt() {
        return inProgressAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public int getAlertsSent() {
        return alertsSent;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public void setMother(Mother mother) {
        this.mother = mother;
    }

    public void setAcceptedBy(Volunteer acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public void setInProgressAt(LocalDateTime inProgressAt) {
        this.inProgressAt = inProgressAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public void setAlertsSent(int alertsSent) {
        this.alertsSent = alertsSent;
    }

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

    public static HelpRequestBuilder builder() {
        return new HelpRequestBuilder();
    }

    public static class HelpRequestBuilder {
        private Long id;
        private String caseId;
        private Mother mother;
        private Volunteer acceptedBy;
        private RequestType requestType;
        private RequestStatus status = RequestStatus.PENDING;
        private String zone;
        private RiskLevel riskLevel;
        private LocalDate dueDate;
        private String notes;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime acceptedAt;
        private LocalDateTime inProgressAt;
        private LocalDateTime closedAt;
        private int alertsSent = 0;

        public HelpRequestBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public HelpRequestBuilder caseId(String caseId) {
            this.caseId = caseId;
            return this;
        }

        public HelpRequestBuilder mother(Mother mother) {
            this.mother = mother;
            return this;
        }

        public HelpRequestBuilder acceptedBy(Volunteer acceptedBy) {
            this.acceptedBy = acceptedBy;
            return this;
        }

        public HelpRequestBuilder requestType(RequestType requestType) {
            this.requestType = requestType;
            return this;
        }

        public HelpRequestBuilder status(RequestStatus status) {
            this.status = status;
            return this;
        }

        public HelpRequestBuilder zone(String zone) {
            this.zone = zone;
            return this;
        }

        public HelpRequestBuilder riskLevel(RiskLevel riskLevel) {
            this.riskLevel = riskLevel;
            return this;
        }

        public HelpRequestBuilder dueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public HelpRequestBuilder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public HelpRequestBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public HelpRequestBuilder acceptedAt(LocalDateTime acceptedAt) {
            this.acceptedAt = acceptedAt;
            return this;
        }

        public HelpRequestBuilder inProgressAt(LocalDateTime inProgressAt) {
            this.inProgressAt = inProgressAt;
            return this;
        }

        public HelpRequestBuilder closedAt(LocalDateTime closedAt) {
            this.closedAt = closedAt;
            return this;
        }

        public HelpRequestBuilder alertsSent(int alertsSent) {
            this.alertsSent = alertsSent;
            return this;
        }

        public HelpRequest build() {
            return new HelpRequest(id, caseId, mother, acceptedBy, requestType, status, zone,
                    riskLevel, dueDate, notes, createdAt, acceptedAt, inProgressAt, closedAt, alertsSent);
        }
    }
}
