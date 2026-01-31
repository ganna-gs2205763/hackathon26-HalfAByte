package com.safebirth.domain.volunteer;

import com.safebirth.domain.mother.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a registered volunteer in the system.
 * Volunteers receive alerts for emergencies matching their skills.
 */
@Entity
@Table(name = "volunteers", indexes = {
        @Index(name = "idx_volunteer_phone", columnList = "phoneNumber", unique = true),
        @Index(name = "idx_volunteer_status", columnList = "status")
})
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

    /**
     * Camp where volunteer is based (for backward compatibility).
     */
    @Column(length = 50)
    private String camp;

    /**
     * Volunteer's profession (new field).
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Profession profession;

    /**
     * Skill type for backward compatibility.
     *
     * @deprecated Use profession and skill flags instead
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SkillType skillType;

    // Skill flags - which types of emergencies can this volunteer assist with?

    @Column
    private Boolean canAssistLabor = false;

    @Column
    private Boolean canAssistBleeding = false;

    @Column
    private Boolean canAssistPainFever = false;

    @Column
    private Boolean canAssistBabyMovement = false;

    @Column
    private Boolean canGiveAdvice = false;

    /**
     * Zones this volunteer covers (for backward compatibility).
     *
     * @deprecated Zone-based matching removed, now skill-based
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "volunteer_zones", joinColumns = @JoinColumn(name = "volunteer_id"))
    @Column(name = "zone", length = 20)
    private Set<String> zones = new HashSet<>();

    /**
     * Current status (AVAILABLE or BUSY).
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private AvailabilityStatus status = AvailabilityStatus.AVAILABLE;

    /**
     * Current case ID when status is BUSY.
     */
    @Column(length = 20)
    private String currentCaseId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Language preferredLanguage = Language.ENGLISH;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column
    private LocalDateTime lastActiveAt;

    /**
     * Number of cases successfully completed by this volunteer.
     */
    @Column
    private int completedCases = 0;

    public Volunteer() {
    }

    public Volunteer(Long id, String phoneNumber, String name, String camp, Profession profession,
                     SkillType skillType, Boolean canAssistLabor, Boolean canAssistBleeding,
                     Boolean canAssistPainFever, Boolean canAssistBabyMovement, Boolean canGiveAdvice,
                     Set<String> zones, AvailabilityStatus status, String currentCaseId,
                     Language preferredLanguage, LocalDateTime registeredAt, LocalDateTime lastActiveAt,
                     int completedCases) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.camp = camp;
        this.profession = profession;
        this.skillType = skillType;
        this.canAssistLabor = canAssistLabor != null ? canAssistLabor : false;
        this.canAssistBleeding = canAssistBleeding != null ? canAssistBleeding : false;
        this.canAssistPainFever = canAssistPainFever != null ? canAssistPainFever : false;
        this.canAssistBabyMovement = canAssistBabyMovement != null ? canAssistBabyMovement : false;
        this.canGiveAdvice = canGiveAdvice != null ? canGiveAdvice : false;
        this.zones = zones != null ? zones : new HashSet<>();
        this.status = status != null ? status : AvailabilityStatus.AVAILABLE;
        this.currentCaseId = currentCaseId;
        this.preferredLanguage = preferredLanguage != null ? preferredLanguage : Language.ENGLISH;
        this.registeredAt = registeredAt != null ? registeredAt : LocalDateTime.now();
        this.lastActiveAt = lastActiveAt;
        this.completedCases = completedCases;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getName() {
        return name;
    }

    public String getCamp() {
        return camp;
    }

    public Profession getProfession() {
        return profession;
    }

    public SkillType getSkillType() {
        return skillType;
    }

    public Boolean getCanAssistLabor() {
        return canAssistLabor;
    }

    public Boolean getCanAssistBleeding() {
        return canAssistBleeding;
    }

    public Boolean getCanAssistPainFever() {
        return canAssistPainFever;
    }

    public Boolean getCanAssistBabyMovement() {
        return canAssistBabyMovement;
    }

    public Boolean getCanGiveAdvice() {
        return canGiveAdvice;
    }

    public Set<String> getZones() {
        return zones;
    }

    public AvailabilityStatus getStatus() {
        return status;
    }

    public String getCurrentCaseId() {
        return currentCaseId;
    }

    public Language getPreferredLanguage() {
        return preferredLanguage;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public int getCompletedCases() {
        return completedCases;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCamp(String camp) {
        this.camp = camp;
    }

    public void setProfession(Profession profession) {
        this.profession = profession;
    }

    public void setSkillType(SkillType skillType) {
        this.skillType = skillType;
    }

    public void setCanAssistLabor(Boolean canAssistLabor) {
        this.canAssistLabor = canAssistLabor;
    }

    public void setCanAssistBleeding(Boolean canAssistBleeding) {
        this.canAssistBleeding = canAssistBleeding;
    }

    public void setCanAssistPainFever(Boolean canAssistPainFever) {
        this.canAssistPainFever = canAssistPainFever;
    }

    public void setCanAssistBabyMovement(Boolean canAssistBabyMovement) {
        this.canAssistBabyMovement = canAssistBabyMovement;
    }

    public void setCanGiveAdvice(Boolean canGiveAdvice) {
        this.canGiveAdvice = canGiveAdvice;
    }

    public void setZones(Set<String> zones) {
        this.zones = zones;
    }

    public void setStatus(AvailabilityStatus status) {
        this.status = status;
    }

    public void setCurrentCaseId(String currentCaseId) {
        this.currentCaseId = currentCaseId;
    }

    public void setPreferredLanguage(Language preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public void setLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    public void setCompletedCases(int completedCases) {
        this.completedCases = completedCases;
    }

    /**
     * Alias for status - for backward compatibility.
     *
     * @deprecated Use status instead
     */
    public AvailabilityStatus getAvailability() {
        return status;
    }

    /**
     * Alias for status - for backward compatibility.
     *
     * @deprecated Use setStatus instead
     */
    public void setAvailability(AvailabilityStatus availability) {
        this.status = availability;
    }

    /**
     * Generate a human-readable volunteer ID.
     *
     * @return formatted ID like "V-0001"
     */
    public String getFormattedId() {
        return String.format("V-%04d", id);
    }

    /**
     * Check if this volunteer covers a specific zone (backward compatibility).
     *
     * @deprecated Zone-based matching removed
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
        return status == AvailabilityStatus.AVAILABLE;
    }

    /**
     * Check if volunteer has all required registration fields.
     *
     * @return true if fully registered
     */
    public boolean isFullyRegistered() {
        // Support both old (skillType) and new (profession + flags) model
        boolean hasOldModel = skillType != null;
        boolean hasNewModel = profession != null
                && (Boolean.TRUE.equals(canAssistLabor) || Boolean.TRUE.equals(canAssistBleeding)
                        || Boolean.TRUE.equals(canAssistPainFever) || Boolean.TRUE.equals(canAssistBabyMovement)
                        || Boolean.TRUE.equals(canGiveAdvice));
        return hasOldModel || hasNewModel;
    }

    /**
     * Check if this volunteer can assist with a given request type.
     *
     * @param requestType the request type to check
     * @return true if volunteer has the matching skill
     */
    public boolean canAssist(String requestType) {
        if (requestType == null)
            return false;
        return switch (requestType.toUpperCase()) {
            case "LABOR" -> Boolean.TRUE.equals(canAssistLabor);
            case "BLEEDING" -> Boolean.TRUE.equals(canAssistBleeding);
            case "PAIN_FEVER" -> Boolean.TRUE.equals(canAssistPainFever);
            case "BABY_MOVEMENT" -> Boolean.TRUE.equals(canAssistBabyMovement);
            case "ADVICE" -> Boolean.TRUE.equals(canGiveAdvice);
            case "OTHER" -> true; // Any volunteer can help with OTHER
            default -> false;
        };
    }

    @PrePersist
    protected void onCreate() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
    }

    public static VolunteerBuilder builder() {
        return new VolunteerBuilder();
    }

    public VolunteerBuilder toBuilder() {
        return new VolunteerBuilder()
                .id(this.id)
                .phoneNumber(this.phoneNumber)
                .name(this.name)
                .camp(this.camp)
                .profession(this.profession)
                .skillType(this.skillType)
                .canAssistLabor(this.canAssistLabor)
                .canAssistBleeding(this.canAssistBleeding)
                .canAssistPainFever(this.canAssistPainFever)
                .canAssistBabyMovement(this.canAssistBabyMovement)
                .canGiveAdvice(this.canGiveAdvice)
                .zones(this.zones)
                .status(this.status)
                .currentCaseId(this.currentCaseId)
                .preferredLanguage(this.preferredLanguage)
                .registeredAt(this.registeredAt)
                .lastActiveAt(this.lastActiveAt)
                .completedCases(this.completedCases);
    }

    public static class VolunteerBuilder {
        private Long id;
        private String phoneNumber;
        private String name;
        private String camp;
        private Profession profession;
        private SkillType skillType;
        private Boolean canAssistLabor = false;
        private Boolean canAssistBleeding = false;
        private Boolean canAssistPainFever = false;
        private Boolean canAssistBabyMovement = false;
        private Boolean canGiveAdvice = false;
        private Set<String> zones = new HashSet<>();
        private AvailabilityStatus status = AvailabilityStatus.AVAILABLE;
        private String currentCaseId;
        private Language preferredLanguage = Language.ENGLISH;
        private LocalDateTime registeredAt = LocalDateTime.now();
        private LocalDateTime lastActiveAt;
        private int completedCases = 0;

        public VolunteerBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public VolunteerBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public VolunteerBuilder name(String name) {
            this.name = name;
            return this;
        }

        public VolunteerBuilder camp(String camp) {
            this.camp = camp;
            return this;
        }

        public VolunteerBuilder profession(Profession profession) {
            this.profession = profession;
            return this;
        }

        public VolunteerBuilder skillType(SkillType skillType) {
            this.skillType = skillType;
            return this;
        }

        public VolunteerBuilder canAssistLabor(Boolean canAssistLabor) {
            this.canAssistLabor = canAssistLabor;
            return this;
        }

        public VolunteerBuilder canAssistBleeding(Boolean canAssistBleeding) {
            this.canAssistBleeding = canAssistBleeding;
            return this;
        }

        public VolunteerBuilder canAssistPainFever(Boolean canAssistPainFever) {
            this.canAssistPainFever = canAssistPainFever;
            return this;
        }

        public VolunteerBuilder canAssistBabyMovement(Boolean canAssistBabyMovement) {
            this.canAssistBabyMovement = canAssistBabyMovement;
            return this;
        }

        public VolunteerBuilder canGiveAdvice(Boolean canGiveAdvice) {
            this.canGiveAdvice = canGiveAdvice;
            return this;
        }

        public VolunteerBuilder zones(Set<String> zones) {
            this.zones = zones;
            return this;
        }

        public VolunteerBuilder status(AvailabilityStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Alias for status() for backward compatibility.
         *
         * @deprecated Use status() instead
         */
        @Deprecated
        public VolunteerBuilder availability(AvailabilityStatus status) {
            this.status = status;
            return this;
        }

        public VolunteerBuilder currentCaseId(String currentCaseId) {
            this.currentCaseId = currentCaseId;
            return this;
        }

        public VolunteerBuilder preferredLanguage(Language preferredLanguage) {
            this.preferredLanguage = preferredLanguage;
            return this;
        }

        public VolunteerBuilder registeredAt(LocalDateTime registeredAt) {
            this.registeredAt = registeredAt;
            return this;
        }

        public VolunteerBuilder lastActiveAt(LocalDateTime lastActiveAt) {
            this.lastActiveAt = lastActiveAt;
            return this;
        }

        public VolunteerBuilder completedCases(int completedCases) {
            this.completedCases = completedCases;
            return this;
        }

        public Volunteer build() {
            return new Volunteer(id, phoneNumber, name, camp, profession, skillType, canAssistLabor,
                    canAssistBleeding, canAssistPainFever, canAssistBabyMovement, canGiveAdvice,
                    zones, status, currentCaseId, preferredLanguage, registeredAt, lastActiveAt, completedCases);
        }
    }
}
