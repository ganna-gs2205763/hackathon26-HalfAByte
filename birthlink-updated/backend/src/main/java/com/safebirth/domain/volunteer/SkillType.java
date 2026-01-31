package com.safebirth.domain.volunteer;

/**
 * Skill types for volunteers.
 * Used for matching priority during emergencies.
 */
public enum SkillType {
    
    /**
     * Certified midwife - highest priority for birth assistance.
     */
    MIDWIFE(1),
    
    /**
     * Registered nurse - high priority.
     */
    NURSE(2),
    
    /**
     * Trained birth attendant - medium priority.
     */
    TRAINED_ATTENDANT(3),
    
    /**
     * Community health worker - lower priority.
     */
    COMMUNITY_HEALTH_WORKER(4),
    
    /**
     * General community volunteer - lowest priority.
     */
    COMMUNITY_VOLUNTEER(5);

    private final int priority;

    SkillType(int priority) {
        this.priority = priority;
    }

    /**
     * Get the matching priority (lower = higher priority).
     *
     * @return the priority value
     */
    public int getPriority() {
        return priority;
    }
}
