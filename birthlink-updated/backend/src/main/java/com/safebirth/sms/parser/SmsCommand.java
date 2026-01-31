package com.safebirth.sms.parser;

import com.safebirth.domain.mother.Language;

import java.util.Map;

/**
 * Represents a parsed SMS command with extracted parameters.
 * Uses Java 17 record for immutability and compact syntax.
 *
 * Parameters map may contain:
 * - For REGISTER_MOTHER: camp, zone, dueDate, riskLevel
 * - For REGISTER_VOLUNTEER: name, camp, zones, skillType
 * - For case commands (ACCEPT, COMPLETE, CANCEL): caseId
 */
public record SmsCommand(
        CommandType type,
        String senderPhone,
        Language detectedLanguage,
        String rawMessage,
        Map<String, String> parameters
) {
    
    /**
     * Get a parameter value by key.
     *
     * @param key the parameter key
     * @return the parameter value or null if not present
     */
    public String getParameter(String key) {
        return parameters != null ? parameters.get(key) : null;
    }

    /**
     * Check if this command has a specific parameter.
     *
     * @param key the parameter key
     * @return true if the parameter exists
     */
    public boolean hasParameter(String key) {
        return parameters != null && parameters.containsKey(key);
    }

    /**
     * Get the case ID from the command parameters.
     * Commonly used for ACCEPT, COMPLETE, and CANCEL commands.
     *
     * @return the case ID (e.g., "HR-0042") or null if not present
     */
    public String getCaseId() {
        return getParameter("caseId");
    }

    /**
     * Get the camp from registration parameters.
     *
     * @return the camp name or null if not present
     */
    public String getCamp() {
        return getParameter("camp");
    }

    /**
     * Get the zone from mother registration parameters.
     *
     * @return the zone identifier or null if not present
     */
    public String getZone() {
        return getParameter("zone");
    }

    /**
     * Get the zones from volunteer registration parameters.
     * May be comma-separated.
     *
     * @return the zones string or null if not present
     */
    public String getZones() {
        return getParameter("zones");
    }

    /**
     * Get the name from volunteer registration parameters.
     *
     * @return the volunteer name or null if not present
     */
    public String getName() {
        return getParameter("name");
    }

    /**
     * Get the due date from mother registration parameters.
     * Format: "dd-mm-yyyy"
     *
     * @return the due date string or null if not present
     */
    public String getDueDate() {
        return getParameter("dueDate");
    }

    /**
     * Get the risk level from mother registration parameters.
     *
     * @return the risk level string (HIGH, MEDIUM, LOW) or null if not present
     */
    public String getRiskLevel() {
        return getParameter("riskLevel");
    }

    /**
     * Get the skill type from volunteer registration parameters.
     *
     * @return the skill type string or null if not present
     */
    public String getSkillType() {
        return getParameter("skillType");
    }

    /**
     * Check if this is a registration command.
     *
     * @return true if this is a mother or volunteer registration
     */
    public boolean isRegistration() {
        return type == CommandType.REGISTER_MOTHER || type == CommandType.REGISTER_VOLUNTEER;
    }

    /**
     * Check if this is an emergency or urgent command.
     *
     * @return true if this is an emergency
     */
    public boolean isEmergency() {
        return type == CommandType.EMERGENCY;
    }

    /**
     * Check if this is a case management command.
     *
     * @return true if this is accept, complete, or cancel
     */
    public boolean isCaseManagement() {
        return type == CommandType.ACCEPT_CASE || 
               type == CommandType.COMPLETE_CASE || 
               type == CommandType.CANCEL_CASE;
    }

    /**
     * Check if this is an availability status command.
     *
     * @return true if this is available, busy, or offline
     */
    public boolean isAvailabilityCommand() {
        return type == CommandType.AVAILABLE || 
               type == CommandType.BUSY || 
               type == CommandType.OFFLINE;
    }

    /**
     * Check if this command was recognized.
     *
     * @return true if the command type is not UNKNOWN
     */
    public boolean isRecognized() {
        return type != CommandType.UNKNOWN;
    }
}
