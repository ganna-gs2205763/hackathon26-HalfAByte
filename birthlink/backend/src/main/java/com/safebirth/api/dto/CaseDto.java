package com.safebirth.api.dto;

import com.safebirth.domain.helprequest.HelpRequest;
import com.safebirth.domain.helprequest.RequestStatus;
import com.safebirth.domain.helprequest.RequestType;
import com.safebirth.domain.mother.RiskLevel;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Case details DTO for the Flutter app.
 */
@Builder
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
        return CaseDto.builder()
                .caseId(request.getCaseId())
                .requestType(request.getRequestType())
                .status(request.getStatus())
                .zone(request.getZone())
                .riskLevel(request.getRiskLevel())
                .dueDate(request.getDueDate())
                .motherPhone(maskPhone(request.getMother().getPhoneNumber()))
                .motherName(request.getMother().getName())
                .volunteerPhone(request.getAcceptedBy() != null 
                        ? maskPhone(request.getAcceptedBy().getPhoneNumber()) : null)
                .volunteerName(request.getAcceptedBy() != null 
                        ? request.getAcceptedBy().getName() : null)
                .createdAt(request.getCreatedAt())
                .acceptedAt(request.getAcceptedAt())
                .closedAt(request.getClosedAt())
                .notes(request.getNotes())
                .build();
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, phone.length() - 4) + "****";
    }
}
