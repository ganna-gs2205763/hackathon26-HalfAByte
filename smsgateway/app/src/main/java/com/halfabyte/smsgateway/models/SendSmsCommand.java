package com.halfabyte.smsgateway.models;

import java.util.List;

/**
 * Represents a command from the backend to send SMS messages.
 */
public class SendSmsCommand {
    private String type;
    private String request_id;
    private List<String> recipients;
    private String message;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRequestId() {
        return request_id;
    }

    public void setRequestId(String requestId) {
        this.request_id = requestId;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
