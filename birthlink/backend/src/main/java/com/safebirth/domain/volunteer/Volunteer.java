package com.safebirth.domain.volunteer;

import com.safebirth.domain.mother.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a registered volunteer in the system.
 * Volunteers receive alerts for emergencies in their coverage zones.
 */
@Entity
@Table(name = "volunteers", indexes = {
        @Index(name = "idx_volunteer_phone", columnList = "phoneNumber", unique = true),
        @Index(name = "idx_volunteer_availability", columnList = "availability")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Volunteer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Column(nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(length = 100)
    private String name;

    @NotBlank(message = "Camp is required")
    @Column(nullable = false, length = 50)
    private String camp;

    @NotNull(message = "Skill type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SkillType skillType;

    /**
     * Zones this volunteer covers.
     * Stored as comma-separated values for simplicity with H2.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "volunteer_zones", joinColumns = @JoinColumn(name = "volunteer_id"))
    @Column(name = "zone", length = 20)
    @Builder.Default
    private Set<String> zones = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private AvailabilityStatus availability = AvailabilityStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private Language preferredLanguage = Language.ENGLISH;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column
    private LocalDateTime lastActiveAt;

    /**
     * Number of cases successfully completed by this volunteer.
     */
    @Column
    @Builder.Default
    private int completedCases = 0;

    /**
     * Generate a human-readable volunteer ID.
     *
     * @return formatted ID like "V-0001"
     */
    public String getFormattedId() {
        return String.format("V-%04d", id);
    }

    /**
     * Check if this volunteer covers a specific zone.
     *
     * @param zone the zone to check
     * @return true if the volunteer covers this zone
     */
    public boolean coversZone(String zone) {
        return zones != null && zones.contains(zone);
    }

    /**
     * Check if this volunteer is currently available.
     *
     * @return true if available
     */
    public boolean isAvailable() {
        return availability == AvailabilityStatus.AVAILABLE;
    }

    @PrePersist
    protected void onCreate() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
    }
}
