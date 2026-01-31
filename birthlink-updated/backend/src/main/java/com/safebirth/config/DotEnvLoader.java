package com.safebirth.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads environment variables from .env file into Spring properties.
 * This allows local development without setting system environment variables.
 */
@Component
public class DotEnvLoader implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Path envFile = Path.of(".env");
        if (!Files.exists(envFile)) {
            return;
        }

        try {
            Map<String, Object> envVars = new HashMap<>();
            Files.readAllLines(envFile).forEach(line -> {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    return;
                }
                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    String key = line.substring(0, eqIdx).trim();
                    String value = line.substring(eqIdx + 1).trim();
                    // Remove quotes if present
                    if ((value.startsWith("\"") && value.endsWith("\"")) ||
                            (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }
                    envVars.put(key, value);
                }
            });

            if (!envVars.isEmpty()) {
                applicationContext.getEnvironment().getPropertySources()
                        .addFirst(new MapPropertySource("dotenv", envVars));
                System.out.println("✅ Loaded " + envVars.size() + " properties from .env file");
            }
        } catch (IOException e) {
            System.err.println("⚠️ Failed to load .env file: " + e.getMessage());
        }
    }
}
