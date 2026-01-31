package com.safebirth.domain.helprequest;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Tracks volunteer responses to help request alerts.
 * Used for ETA-based matching.
 */
@Entity
@Table(name = "volunteer_responses", indexes = {
        @Index(name = "idx_response_case", columnList = "caseId")
})
public class VolunteerResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The case/help request ID.
     */
    @Column(nullable = false, length = 20)
    private String caseId;

    /**
     * The volunteer who responded.
     */
    @Column(nullable = false)
    private Long volunteerId;

    /**
     * ETA in minutes provided by volunteer.
     */
    @Column(nullable = false)
    private Integer etaMinutes;

    /**
     * When the response was received.
     */
    @Column(nullable = false)
    private LocalDateTime respondedAt = LocalDateTime.now();

    /**
     * Whether this volunteer was selected for the case.
     */
    @Column
    private Boolean selected = false;

    public VolunteerResponse() {
    }

    public VolunteerResponse(Long id, String caseId, Long volunteerId, Integer etaMinutes,
                             LocalDateTime respondedAt, Boolean selected) {
        this.id = id;
        this.caseId = caseId;
        this.volunteerId = volunteerId;
        this.etaMinutes = etaMinutes;
        this.respondedAt = respondedAt != null ? respondedAt : LocalDateTime.now();
        this.selected = selected != null ? selected : false;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getCaseId() {
        return caseId;
    }

    public Long getVolunteerId() {
        return volunteerId;
    }

    public Integer getEtaMinutes() {
        return etaMinutes;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public Boolean getSelected() {
        return selected;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public void setVolunteerId(Long volunteerId) {
        this.volunteerId = volunteerId;
    }

    public void setEtaMinutes(Integer etaMinutes) {
        this.etaMinutes = etaMinutes;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    @PrePersist
    protected void onCreate() {
        if (respondedAt == null) {
            respondedAt = LocalDateTime.now();
        }
    }

    public static VolunteerResponseBuilder builder() {
        return new VolunteerResponseBuilder();
    }

    public static class VolunteerResponseBuilder {
        private Long id;
        private String caseId;
        private Long volunteerId;
        private Integer etaMinutes;
        private LocalDateTime respondedAt = LocalDateTime.now();
        private Boolean selected = false;

        public VolunteerResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public VolunteerResponseBuilder caseId(String caseId) {
            this.caseId = caseId;
            return this;
        }

        public VolunteerResponseBuilder volunteerId(Long volunteerId) {
            this.volunteerId = volunteerId;
            return this;
        }

        public VolunteerResponseBuilder etaMinutes(Integer etaMinutes) {
            this.etaMinutes = etaMinutes;
            return this;
        }

        public VolunteerResponseBuilder respondedAt(LocalDateTime respondedAt) {
            this.respondedAt = respondedAt;
            return this;
        }

        public VolunteerResponseBuilder selected(Boolean selected) {
            this.selected = selected;
            return this;
        }

        public VolunteerResponse build() {
            return new VolunteerResponse(id, caseId, volunteerId, etaMinutes, respondedAt, selected);
        }
    }
}
