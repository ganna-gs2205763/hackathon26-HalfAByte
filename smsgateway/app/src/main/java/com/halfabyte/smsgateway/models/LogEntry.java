package com.halfabyte.smsgateway.models;

/**
 * Represents a log entry for the activity log.
 */
public class LogEntry {
    public enum Type {
        INCOMING, // Incoming SMS received
        OUTGOING, // SMS sent
        ERROR, // Error occurred
        SYSTEM // System message (connected, disconnected, etc.)
    }

    private Type type;
    private String message;
    private long timestamp;

    public LogEntry(Type type, String message) {
        this.type = type;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
