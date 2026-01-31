package com.halfabyte.smsgateway.models;

/**
 * Represents an incoming SMS message to be sent to the backend.
 */
public class IncomingSmsMessage {
    private final String type = "incoming_sms";
    private String sender;
    private String message;
    private long timestamp;

    public IncomingSmsMessage(String sender, String message) {
        this.sender = sender;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public String getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
