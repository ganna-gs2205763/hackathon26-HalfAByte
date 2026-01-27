package com.safebirth.api.dto;

import com.safebirth.domain.volunteer.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for updating volunteer availability status.
 */
public record AvailabilityUpdateRequest(
        @NotNull(message = "Availability status is required")
        AvailabilityStatus availability
) {}
