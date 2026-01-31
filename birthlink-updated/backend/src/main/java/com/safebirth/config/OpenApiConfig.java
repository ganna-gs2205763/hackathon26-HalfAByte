package com.safebirth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger documentation configuration.
 * Access Swagger UI at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SafeBirth Connect API")
                        .description("""
                                SMS-first maternal support coordination system for crisis settings.
                                
                                ## Overview
                                SafeBirth Connect provides:
                                - SMS-based registration for mothers and volunteers
                                - Emergency help request coordination
                                - Real-time volunteer matching
                                - Dashboard for NGO coordinators
                                
                                ## Authentication
                                - **SMS Endpoints**: No authentication (uses phone number as identifier)
                                - **Dashboard API**: No authentication for POC (add JWT in production)
                                - **Volunteer API**: Uses `X-Phone-Number` header for identification (POC only)
                                
                                ## Bilingual Support
                                Supports Arabic and English for SMS commands and responses.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SafeBirth Connect Team")
                                .email("support@safebirth.example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server")
                ))
                .components(new Components()
                        .addSecuritySchemes("phoneNumber", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Phone-Number")
                                .description("Volunteer's phone number for identification (POC only)")));
    }

    /**
     * Add X-Phone-Number header parameter to volunteer endpoints.
     */
    @Bean
    public OperationCustomizer volunteerOperationCustomizer() {
        return (operation, handlerMethod) -> {
            // Add security requirement for volunteer endpoints
            if (handlerMethod.getBeanType().getSimpleName().equals("VolunteerController")) {
                operation.addSecurityItem(new SecurityRequirement().addList("phoneNumber"));
            }
            return operation;
        };
    }
}
