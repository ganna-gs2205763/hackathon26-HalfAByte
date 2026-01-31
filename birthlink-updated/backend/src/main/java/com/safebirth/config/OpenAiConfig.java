package com.safebirth.config;

import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for OpenAI API integration.
 * Creates the OpenAiService bean when the API key is available.
 */
@Configuration
public class OpenAiConfig {

    private static final Logger log = LoggerFactory.getLogger(OpenAiConfig.class);

    @Value("${openai.api-key:${OPENAI_API_KEY:}}")
    private String apiKey;

    @Value("${openai.timeout-seconds:60}")
    private int timeoutSeconds;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.max-tokens:300}")
    private int maxTokens;

    @Value("${openai.temperature:0.7}")
    private double temperature;

    public String getApiKey() {
        return apiKey;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public String getModel() {
        return model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    /**
     * Creates the OpenAiService bean.
     * Only created when openai.api-key is set or OPENAI_API_KEY env var exists.
     */
    @Bean
    @ConditionalOnProperty(name = "openai.enabled", havingValue = "true", matchIfMissing = true)
    public OpenAiService openAiService() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OpenAI API key not configured. Using mock AI responses.");
            log.warn("Set OPENAI_API_KEY environment variable or openai.api-key property.");
            return null;
        }

        log.info("OpenAI API configured: model={}, maxTokens={}, timeout={}s", model, maxTokens, timeoutSeconds);
        return new OpenAiService(apiKey, Duration.ofSeconds(timeoutSeconds));
    }
}
