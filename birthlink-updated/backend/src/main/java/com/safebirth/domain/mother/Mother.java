package com.safebirth.domain.mother;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a registered mother in the system.
 * Mothers can send SMS to register and request emergency help.
 */
@Entity
@Table(name = "mothers", indexes = {
        @Index(name = "idx_mother_phone", columnList = "phoneNumber", unique = true),
        @Index(name = "idx_mother_zone", columnList = "zone")
})
public class Mother {

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
     * Mother's age (persisted across sessions).
     */
    @Column
    private Integer age;

    @Column(length = 50)
    private String camp;

    @Column(length = 20)
    private String zone;

    /**
     * Expected due date (persisted across sessions).
     */
    @Column
    private LocalDate dueDate;

    /**
     * Whether mother has previous pregnancy complications (persisted).
     */
    @Column
    private Boolean prevComplications = false;

    /**
     * Risk level for backwards compatibility.
     *
     * @deprecated Use prevComplications instead
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private RiskLevel riskLevel = RiskLevel.LOW;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Language preferredLanguage = Language.ENGLISH;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column
    private LocalDateTime lastContactAt;

    public Mother() {
    }

    public Mother(Long id, String phoneNumber, String name, Integer age, String camp, String zone,
                  LocalDate dueDate, Boolean prevComplications, RiskLevel riskLevel,
                  Language preferredLanguage, LocalDateTime registeredAt, LocalDateTime lastContactAt) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.age = age;
        this.camp = camp;
        this.zone = zone;
        this.dueDate = dueDate;
        this.prevComplications = prevComplications != null ? prevComplications : false;
        this.riskLevel = riskLevel != null ? riskLevel : RiskLevel.LOW;
        this.preferredLanguage = preferredLanguage != null ? preferredLanguage : Language.ENGLISH;
        this.registeredAt = registeredAt != null ? registeredAt : LocalDateTime.now();
        this.lastContactAt = lastContactAt;
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

    public Integer getAge() {
        return age;
    }

    public String getCamp() {
        return camp;
    }

    public String getZone() {
        return zone;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Boolean getPrevComplications() {
        return prevComplications;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public Language getPreferredLanguage() {
        return preferredLanguage;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public LocalDateTime getLastContactAt() {
        return lastContactAt;
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

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setCamp(String camp) {
        this.camp = camp;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setPrevComplications(Boolean prevComplications) {
        this.prevComplications = prevComplications;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public void setPreferredLanguage(Language preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public void setLastContactAt(LocalDateTime lastContactAt) {
        this.lastContactAt = lastContactAt;
    }

    /**
     * Generate a human-readable mother ID.
     *
     * @return formatted ID like "M-0001"
     */
    public String getFormattedId() {
        return String.format("M-%04d", id);
    }

    /**
     * Check if mother has all required registration fields.
     * Used to determine if we should treat as new registration.
     *
     * @return true if all required fields are present
     */
    public boolean isFullyRegistered() {
        return age != null
                && dueDate != null
                && camp != null && !camp.isBlank()
                && zone != null && !zone.isBlank();
    }

    @PrePersist
    protected void onCreate() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
    }

    public static MotherBuilder builder() {
        return new MotherBuilder();
    }

    public static class MotherBuilder {
        private Long id;
        private String phoneNumber;
        private String name;
        private Integer age;
        private String camp;
        private String zone;
        private LocalDate dueDate;
        private Boolean prevComplications = false;
        private RiskLevel riskLevel = RiskLevel.LOW;
        private Language preferredLanguage = Language.ENGLISH;
        private LocalDateTime registeredAt = LocalDateTime.now();
        private LocalDateTime lastContactAt;

        public MotherBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MotherBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public MotherBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MotherBuilder age(Integer age) {
            this.age = age;
            return this;
        }

        public MotherBuilder camp(String camp) {
            this.camp = camp;
            return this;
        }

        public MotherBuilder zone(String zone) {
            this.zone = zone;
            return this;
        }

        public MotherBuilder dueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public MotherBuilder prevComplications(Boolean prevComplications) {
            this.prevComplications = prevComplications;
            return this;
        }

        public MotherBuilder riskLevel(RiskLevel riskLevel) {
            this.riskLevel = riskLevel;
            return this;
        }

        public MotherBuilder preferredLanguage(Language preferredLanguage) {
            this.preferredLanguage = preferredLanguage;
            return this;
        }

        public MotherBuilder registeredAt(LocalDateTime registeredAt) {
            this.registeredAt = registeredAt;
            return this;
        }

        public MotherBuilder lastContactAt(LocalDateTime lastContactAt) {
            this.lastContactAt = lastContactAt;
            return this;
        }

        public Mother build() {
            return new Mother(id, phoneNumber, name, age, camp, zone, dueDate, prevComplications,
                    riskLevel, preferredLanguage, registeredAt, lastContactAt);
        }
    }
}
