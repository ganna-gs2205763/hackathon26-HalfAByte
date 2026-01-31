package com.safebirth.sms.conversation;

/**
 * Types of conversations for state tracking.
 */
public enum ConversationType {
    /**
     * Initial contact, determining if mother or volunteer.
     */
    ROLE_DETECTION,

    /**
     * Registering a new mother.
     */
    MOTHER_REGISTRATION,

    /**
     * Registering a new volunteer.
     */
    VOLUNTEER_REGISTRATION,

    /**
     * Mother requesting help (determining request type).
     */
    HELP_REQUEST,

    /**
     * General conversation or query.
     */
    GENERAL
}
