package com.safebirth.domain.helprequest;

/**
 * Types of help requests a mother can make.
 * Each type maps to volunteer skills.
 */
public enum RequestType {
    /**
     * Generic emergency (backward compatibility).
     * 
     * @deprecated Use specific types like LABOR, BLEEDING, etc.
     */
    @Deprecated
    EMERGENCY,

    /**
     * Generic support request (backward compatibility).
     * 
     * @deprecated Use ADVICE instead
     */
    @Deprecated
    SUPPORT,

    /**
     * Labor started / contractions / water broke
     */
    LABOR,

    /**
     * Heavy vaginal bleeding
     */
    BLEEDING,

    /**
     * Severe pain, fever, headache
     */
    PAIN_FEVER,

    /**
     * Baby not moving / reduced movement
     */
    BABY_MOVEMENT,

    /**
     * Non-emergency support / advice
     */
    ADVICE,

    /**
     * Unclear or unclassified request
     */
    OTHER;

    /**
     * Check if this is an emergency type (not just advice).
     */
    public boolean isEmergency() {
        return this == EMERGENCY || this == LABOR || this == BLEEDING
                || this == PAIN_FEVER || this == BABY_MOVEMENT;
    }
}
