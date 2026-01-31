package com.safebirth.api.dto;

import com.safebirth.domain.helprequest.HelpRequest;
import com.safebirth.domain.helprequest.RequestStatus;
import com.safebirth.domain.helprequest.RequestType;
import com.safebirth.domain.mother.RiskLevel;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Case details DTO for the Flutter app.
 */
public record CaseDto(
        String caseId,
        RequestType requestType,
        RequestStatus status,
        String zone,
        RiskLevel riskLevel,
        LocalDate dueDate,
        String motherPhone,
        String motherName,
        String volunteerPhone,
        String volunteerName,
        LocalDateTime createdAt,
        LocalDateTime acceptedAt,
        LocalDateTime closedAt,
        String notes
) {
    /**
     * Create a CaseDto from a HelpRequest entity.
     *
     * @param request the help request
     * @return the DTO
     */
    public static CaseDto fromEntity(HelpRequest request) {
        return new CaseDto(
                request.getCaseId(),
                request.getRequestType(),
                request.getStatus(),
                request.getZone(),
                request.getRiskLevel(),
                request.getDueDate(),
                maskPhone(request.getMother().getPhoneNumber()),
                request.getMother().getName(),
                request.getAcceptedBy() != null
                        ? maskPhone(request.getAcceptedBy().getPhoneNumber()) : null,
                request.getAcceptedBy() != null
                        ? request.getAcceptedBy().getName() : null,
                request.getCreatedAt(),
                request.getAcceptedAt(),
                request.getClosedAt(),
                request.getNotes()
        );
    }

    public static CaseDtoBuilder builder() {
        return new CaseDtoBuilder();
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, phone.length() - 4) + "****";
    }

    public static class CaseDtoBuilder {
        private String caseId;
        private RequestType requestType;
        private RequestStatus status;
        private String zone;
        private RiskLevel riskLevel;
        private LocalDate dueDate;
        private String motherPhone;
        private String motherName;
        private String volunteerPhone;
        private String volunteerName;
        private LocalDateTime createdAt;
        private LocalDateTime acceptedAt;
        private LocalDateTime closedAt;
        private String notes;

        public CaseDtoBuilder caseId(String caseId) {
            this.caseId = caseId;
            return this;
        }

        public CaseDtoBuilder requestType(RequestType requestType) {
            this.requestType = requestType;
            return this;
        }

        public CaseDtoBuilder status(RequestStatus status) {
            this.status = status;
            return this;
        }

        public CaseDtoBuilder zone(String zone) {
            this.zone = zone;
            return this;
        }

        public CaseDtoBuilder riskLevel(RiskLevel riskLevel) {
            this.riskLevel = riskLevel;
            return this;
        }

        public CaseDtoBuilder dueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public CaseDtoBuilder motherPhone(String motherPhone) {
            this.motherPhone = motherPhone;
            return this;
        }

        public CaseDtoBuilder motherName(String motherName) {
            this.motherName = motherName;
            return this;
        }

        public CaseDtoBuilder volunteerPhone(String volunteerPhone) {
            this.volunteerPhone = volunteerPhone;
            return this;
        }

        public CaseDtoBuilder volunteerName(String volunteerName) {
            this.volunteerName = volunteerName;
            return this;
        }

        public CaseDtoBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public CaseDtoBuilder acceptedAt(LocalDateTime acceptedAt) {
            this.acceptedAt = acceptedAt;
            return this;
        }

        public CaseDtoBuilder closedAt(LocalDateTime closedAt) {
            this.closedAt = closedAt;
            return this;
        }

        public CaseDtoBuilder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public CaseDto build() {
            return new CaseDto(caseId, requestType, status, zone, riskLevel, dueDate,
                    motherPhone, motherName, volunteerPhone, volunteerName, createdAt, acceptedAt, closedAt, notes);
        }
    }
}
