package com.safebirth.domain.volunteer;

/**
 * Availability status for volunteers.
 */
public enum AvailabilityStatus {
    
    /**
     * Volunteer is available to accept new cases.
     */
    AVAILABLE,
    
    /**
     * Volunteer is currently handling a case.
     */
    BUSY,
    
    /**
     * Volunteer is offline/not accepting cases.
     */
    OFFLINE
}
