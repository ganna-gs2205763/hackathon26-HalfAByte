package com.halfabyte.smsgateway.models;

import java.util.List;

/**
 * Represents a confirmation sent to the backend after sending SMS.
 */
public class SmsSentConfirmation {
    private final String type = "sms_sent";
    private String request_id;
    private List<String> recipients;
    private int success_count;
    private int failure_count;
    private String status;

    public SmsSentConfirmation(String requestId, List<String> recipients, int successCount, int failureCount) {
        this.request_id = requestId;
        this.recipients = recipients;
        this.success_count = successCount;
        this.failure_count = failureCount;
        this.status = failureCount == 0 ? "success" : (successCount == 0 ? "failed" : "partial");
    }

    public String getType() {
        return type;
    }

    public String getRequestId() {
        return request_id;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public int getSuccessCount() {
        return success_count;
    }

    public int getFailureCount() {
        return failure_count;
    }

    public String getStatus() {
        return status;
    }
}
