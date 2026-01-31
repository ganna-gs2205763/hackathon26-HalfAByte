package com.safebirth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for conversation management.
 */
@Configuration
@ConfigurationProperties(prefix = "conversation")
public class ConversationConfig {

    /**
     * Minutes before a conversation expires due to inactivity.
     */
    private int timeoutMinutes = 30;

    /**
     * Minutes to wait for volunteer ETA responses before matching.
     */
    private int matchingWindowMinutes = 5;

    public int getTimeoutMinutes() {
        return timeoutMinutes;
    }

    public void setTimeoutMinutes(int timeoutMinutes) {
        this.timeoutMinutes = timeoutMinutes;
    }

    public int getMatchingWindowMinutes() {
        return matchingWindowMinutes;
    }

    public void setMatchingWindowMinutes(int matchingWindowMinutes) {
        this.matchingWindowMinutes = matchingWindowMinutes;
    }
}
