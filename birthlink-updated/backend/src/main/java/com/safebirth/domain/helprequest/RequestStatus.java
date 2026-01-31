package com.safebirth.domain.helprequest;

/**
 * Status of a help request throughout its lifecycle.
 */
public enum RequestStatus {
    
    /**
     * Request created, waiting for volunteer acceptance.
     */
    PENDING,
    
    /**
     * Volunteer has accepted the request.
     */
    ACCEPTED,
    
    /**
     * Volunteer is actively helping the mother.
     */
    IN_PROGRESS,
    
    /**
     * Help request successfully completed.
     */
    COMPLETED,
    
    /**
     * Request was cancelled (by mother, volunteer, or system).
     */
    CANCELLED,
    
    /**
     * No volunteer available - request escalated.
     */
    ESCALATED
}
