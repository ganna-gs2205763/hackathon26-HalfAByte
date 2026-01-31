package com.safebirth.sms.gateway.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for SMS sent confirmation messages from the Android SMS Gateway app.
 * The app sends this after attempting to deliver SMS messages.
 *
 * Example JSON:
 * {"type": "sms_sent", "request_id": "WS-12345", "recipients": ["+249123456789"],
 *  "success_count": 1, "failure_count": 0, "status": "success"}
 */
public record SmsSentConfirmation(
        @JsonProperty("type") String type,
        @JsonProperty("request_id") String requestId,
        @JsonProperty("recipients") List<String> recipients,
        @JsonProperty("success_count") Integer successCount,
        @JsonProperty("failure_count") Integer failureCount,
        @JsonProperty("status") String status
) {
    public static final String TYPE = "sms_sent";

    public boolean isValid() {
        return TYPE.equals(type) && requestId != null && !requestId.isBlank();
    }

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status) || (failureCount != null && failureCount == 0);
    }
}
