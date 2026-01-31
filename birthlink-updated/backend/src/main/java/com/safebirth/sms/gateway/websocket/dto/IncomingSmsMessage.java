package com.safebirth.sms.gateway.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for incoming SMS messages received from the Android SMS Gateway app.
 * The Android app sends this when it receives an SMS on the device.
 *
 * Example JSON:
 * {"type": "incoming_sms", "sender": "+249123456789", "message": "HELP", "timestamp": 1706554800000}
 */
public record IncomingSmsMessage(
        @JsonProperty("type") String type,
        @JsonProperty("sender") String sender,
        @JsonProperty("message") String message,
        @JsonProperty("timestamp") Long timestamp
) {
    public static final String TYPE = "incoming_sms";

    public boolean isValid() {
        return TYPE.equals(type) && sender != null && !sender.isBlank() && message != null;
    }
}
