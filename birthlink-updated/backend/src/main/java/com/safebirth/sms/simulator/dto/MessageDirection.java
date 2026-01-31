package com.safebirth.sms.simulator.dto;

/**
 * Direction of an SMS message in the simulator.
 */
public enum MessageDirection {
    /** Message sent from user (inbound to system) */
    INBOUND,
    /** Message sent from system to user (outbound) */
    OUTBOUND
}
