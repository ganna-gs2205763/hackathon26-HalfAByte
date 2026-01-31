package com.safebirth.sms.conversation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * Response structure expected from OpenAI.
 */
public class AiResponse {

    /**
     * The SMS reply to send to the user.
     */
    @JsonProperty("reply")
    private String reply;

    /**
     * Data extracted from the user's message.
     */
    @JsonProperty("extracted_data")
    private Map<String, Object> extractedData;

    /**
     * Whether all required data has been collected.
     */
    @JsonProperty("is_complete")
    private boolean complete;

    /**
     * Action to perform (REGISTER_MOTHER, REGISTER_VOLUNTEER, CREATE_HELP_REQUEST).
     */
    @JsonProperty("action")
    private String action;

    public AiResponse() {
    }

    public AiResponse(String reply, Map<String, Object> extractedData, boolean complete, String action) {
        this.reply = reply;
        this.extractedData = extractedData;
        this.complete = complete;
        this.action = action;
    }

    // Getters
    public String getReply() {
        return reply;
    }

    public Map<String, Object> getExtractedData() {
        return extractedData;
    }

    public boolean isComplete() {
        return complete;
    }

    public String getAction() {
        return action;
    }

    // Setters
    public void setReply(String reply) {
        this.reply = reply;
    }

    public void setExtractedData(Map<String, Object> extractedData) {
        this.extractedData = extractedData;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AiResponse that = (AiResponse) o;
        return complete == that.complete &&
                Objects.equals(reply, that.reply) &&
                Objects.equals(extractedData, that.extractedData) &&
                Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reply, extractedData, complete, action);
    }

    @Override
    public String toString() {
        return "AiResponse{" +
                "reply='" + reply + '\'' +
                ", extractedData=" + extractedData +
                ", complete=" + complete +
                ", action='" + action + '\'' +
                '}';
    }

    public static AiResponseBuilder builder() {
        return new AiResponseBuilder();
    }

    public static class AiResponseBuilder {
        private String reply;
        private Map<String, Object> extractedData;
        private boolean complete;
        private String action;

        public AiResponseBuilder reply(String reply) {
            this.reply = reply;
            return this;
        }

        public AiResponseBuilder extractedData(Map<String, Object> extractedData) {
            this.extractedData = extractedData;
            return this;
        }

        public AiResponseBuilder complete(boolean complete) {
            this.complete = complete;
            return this;
        }

        public AiResponseBuilder action(String action) {
            this.action = action;
            return this;
        }

        public AiResponse build() {
            return new AiResponse(reply, extractedData, complete, action);
        }
    }
}
