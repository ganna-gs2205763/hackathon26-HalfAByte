package com.safebirth.sms.conversation;

import java.util.Objects;

/**
 * Result of direct command handling.
 */
public class DirectCommandResult {

    private boolean handled;
    private String response;

    public DirectCommandResult() {
    }

    public DirectCommandResult(boolean handled, String response) {
        this.handled = handled;
        this.response = response;
    }

    // Getters
    public boolean isHandled() {
        return handled;
    }

    public String getResponse() {
        return response;
    }

    // Setters
    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectCommandResult that = (DirectCommandResult) o;
        return handled == that.handled && Objects.equals(response, that.response);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handled, response);
    }

    @Override
    public String toString() {
        return "DirectCommandResult{" +
                "handled=" + handled +
                ", response='" + response + '\'' +
                '}';
    }

    public static DirectCommandResult handled(String response) {
        return new DirectCommandResult(true, response);
    }

    public static DirectCommandResult notHandled() {
        return new DirectCommandResult(false, null);
    }

    public static DirectCommandResultBuilder builder() {
        return new DirectCommandResultBuilder();
    }

    public static class DirectCommandResultBuilder {
        private boolean handled;
        private String response;

        public DirectCommandResultBuilder handled(boolean handled) {
            this.handled = handled;
            return this;
        }

        public DirectCommandResultBuilder response(String response) {
            this.response = response;
            return this;
        }

        public DirectCommandResult build() {
            return new DirectCommandResult(handled, response);
        }
    }
}
