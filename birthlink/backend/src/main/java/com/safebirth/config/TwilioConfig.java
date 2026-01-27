package com.safebirth.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Twilio SMS gateway.
 * Credentials are loaded from application.yml or environment variables.
 */
@Configuration
@ConfigurationProperties(prefix = "twilio")
@Getter
@Setter
@Slf4j
public class TwilioConfig {

    private String accountSid;
    private String authToken;
    private String phoneNumber;
    private boolean mockEnabled;

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
