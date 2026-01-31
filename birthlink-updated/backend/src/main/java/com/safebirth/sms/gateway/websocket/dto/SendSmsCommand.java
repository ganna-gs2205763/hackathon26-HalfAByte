package com.safebirth.sms.gateway.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for outgoing SMS commands sent to the Android SMS Gateway app.
 * The app will send actual SMS messages to the specified recipients.
 *
 * Example JSON:
 * {"type": "send_sms", "request_id": "WS-12345", "recipients": ["+249123456789"], "message": "Help is on the way!"}
 */
public record SendSmsCommand(
        @JsonProperty("type") String type,
        @JsonProperty("request_id") String requestId,
        @JsonProperty("recipients") List<String> recipients,
        @JsonProperty("message") String message
) {
    public static final String TYPE = "send_sms";

    public static SendSmsCommand create(String requestId, List<String> recipients, String message) {
        return new SendSmsCommand(TYPE, requestId, recipients, message);
    }

    public static SendSmsCommand create(String requestId, String recipient, String message) {
        return new SendSmsCommand(TYPE, requestId, List.of(recipient), message);
    }
}
