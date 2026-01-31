package com.safebirth.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Twilio SMS gateway.
 * Credentials are loaded from application.yml or environment variables.
 */
@Configuration
@ConfigurationProperties(prefix = "twilio")
public class TwilioConfig {

    private static final Logger log = LoggerFactory.getLogger(TwilioConfig.class);

    private String accountSid;
    private String authToken;
    private String phoneNumber;
    private boolean mockEnabled;

    public String getAccountSid() {
        return accountSid;
    }

    public void setAccountSid(String accountSid) {
        this.accountSid = accountSid;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isMockEnabled() {
        return mockEnabled;
    }

    public void setMockEnabled(boolean mockEnabled) {
        this.mockEnabled = mockEnabled;
    }

    @PostConstruct
    public void init() {
        if (!mockEnabled && accountSid != null && authToken != null
                && !accountSid.startsWith("your_") && !accountSid.startsWith("test_")) {
            try {
                Twilio.init(accountSid, authToken);
                log.info("Twilio initialized with account SID: {}...",
                        accountSid.substring(0, Math.min(8, accountSid.length())));
            } catch (Exception e) {
                log.warn("Failed to initialize Twilio: {}. Running in mock mode.", e.getMessage());
            }
        } else {
            log.info("Twilio mock mode enabled - no actual SMS will be sent");
        }
    }
}
