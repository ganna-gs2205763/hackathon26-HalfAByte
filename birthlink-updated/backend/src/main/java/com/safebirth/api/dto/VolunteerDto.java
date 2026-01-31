package com.safebirth.api.dto;

import com.safebirth.domain.mother.Language;
import com.safebirth.domain.volunteer.AvailabilityStatus;
import com.safebirth.domain.volunteer.SkillType;
import com.safebirth.domain.volunteer.Volunteer;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Volunteer details DTO for the Flutter app.
 */
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
        return new VolunteerDto(
                volunteer.getId(),
                volunteer.getFormattedId(),
                maskPhone(volunteer.getPhoneNumber()),
                volunteer.getName(),
                volunteer.getCamp(),
                volunteer.getSkillType(),
                volunteer.getZones(),
                volunteer.getAvailability(),
                volunteer.getPreferredLanguage(),
                volunteer.getRegisteredAt(),
                volunteer.getLastActiveAt(),
                volunteer.getCompletedCases()
        );
    }

    public static VolunteerDtoBuilder builder() {
        return new VolunteerDtoBuilder();
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, phone.length() - 4) + "****";
    }

    public static class VolunteerDtoBuilder {
        private Long id;
        private String formattedId;
        private String phoneNumber;
        private String name;
        private String camp;
        private SkillType skillType;
        private Set<String> zones;
        private AvailabilityStatus availability;
        private Language preferredLanguage;
        private LocalDateTime registeredAt;
        private LocalDateTime lastActiveAt;
        private int completedCases;

        public VolunteerDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public VolunteerDtoBuilder formattedId(String formattedId) {
            this.formattedId = formattedId;
            return this;
        }

        public VolunteerDtoBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public VolunteerDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public VolunteerDtoBuilder camp(String camp) {
            this.camp = camp;
            return this;
        }

        public VolunteerDtoBuilder skillType(SkillType skillType) {
            this.skillType = skillType;
            return this;
        }

        public VolunteerDtoBuilder zones(Set<String> zones) {
            this.zones = zones;
            return this;
        }

        public VolunteerDtoBuilder availability(AvailabilityStatus availability) {
            this.availability = availability;
            return this;
        }

        public VolunteerDtoBuilder preferredLanguage(Language preferredLanguage) {
            this.preferredLanguage = preferredLanguage;
            return this;
        }

        public VolunteerDtoBuilder registeredAt(LocalDateTime registeredAt) {
            this.registeredAt = registeredAt;
            return this;
        }

        public VolunteerDtoBuilder lastActiveAt(LocalDateTime lastActiveAt) {
            this.lastActiveAt = lastActiveAt;
            return this;
        }

        public VolunteerDtoBuilder completedCases(int completedCases) {
            this.completedCases = completedCases;
            return this;
        }

        public VolunteerDto build() {
            return new VolunteerDto(id, formattedId, phoneNumber, name, camp, skillType, zones,
                    availability, preferredLanguage, registeredAt, lastActiveAt, completedCases);
        }
    }
}
