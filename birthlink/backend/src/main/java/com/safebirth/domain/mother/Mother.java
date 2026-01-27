package com.safebirth.domain.mother;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @NotBlank(message = "Camp is required")
    @Column(nullable = false, length = 50)
    private String camp;

    @NotBlank(message = "Zone is required")
    @Column(nullable = false, length = 20)
    private String zone;

    @Column
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private RiskLevel riskLevel = RiskLevel.LOW;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private Language preferredLanguage = Language.ENGLISH;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column
    private LocalDateTime lastContactAt;

    /**
     * Generate a human-readable mother ID.
     *
     * @return formatted ID like "M-0001"
     */
    public String getFormattedId() {
        return String.format("M-%04d", id);
    }

    @PrePersist
    protected void onCreate() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
    }
}
