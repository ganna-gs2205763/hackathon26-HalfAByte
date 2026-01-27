package com.safebirth.api.dto;

import com.safebirth.domain.mother.Language;
import com.safebirth.domain.volunteer.AvailabilityStatus;
import com.safebirth.domain.volunteer.SkillType;
import com.safebirth.domain.volunteer.Volunteer;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Volunteer details DTO for the Flutter app.
 */
@Builder
public record VolunteerDto(
        Long id,
        String formattedId,
        String phoneNumber,
        String name,
        String camp,
        SkillType skillType,
        Set<String> zones,
        AvailabilityStatus availability,
        Language preferredLanguage,
        LocalDateTime registeredAt,
        LocalDateTime lastActiveAt,
        int completedCases
) {
    /**
     * Create a VolunteerDto from a Volunteer entity.
     *
     * @param volunteer the volunteer
     * @return the DTO
     */
    public static VolunteerDto fromEntity(Volunteer volunteer) {
        return VolunteerDto.builder()
                .id(volunteer.getId())
                .formattedId(volunteer.getFormattedId())
                .phoneNumber(maskPhone(volunteer.getPhoneNumber()))
                .name(volunteer.getName())
                .camp(volunteer.getCamp())
                .skillType(volunteer.getSkillType())
                .zones(volunteer.getZones())
                .availability(volunteer.getAvailability())
                .preferredLanguage(volunteer.getPreferredLanguage())
                .registeredAt(volunteer.getRegisteredAt())
                .lastActiveAt(volunteer.getLastActiveAt())
                .completedCases(volunteer.getCompletedCases())
                .build();
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, phone.length() - 4) + "****";
    }
}
