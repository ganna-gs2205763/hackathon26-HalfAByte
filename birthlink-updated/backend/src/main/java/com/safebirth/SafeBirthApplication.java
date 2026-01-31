package com.safebirth;

import com.safebirth.config.DotEnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SafeBirth Connect - SMS-first maternal support coordination system.
 * 
 * Designed for crisis settings (refugee camps, disaster zones) where
 * internet access is unreliable but SMS remains available.
 */
@SpringBootApplication
public class SafeBirthApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SafeBirthApplication.class);
        app.addInitializers(new DotEnvLoader());
        app.run(args);
    }
}
