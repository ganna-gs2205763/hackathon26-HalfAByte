package com.safebirth.sms.handler;

import com.safebirth.sms.gateway.SmsGateway;
import com.safebirth.sms.parser.SmsCommand;
import com.safebirth.sms.parser.SmsParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for handling incoming SMS via Twilio webhooks.
 * Also provides a simulation endpoint for development testing.
 */
@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
@Slf4j
public class SmsWebhookController {

    private final SmsParser smsParser;
    private final SmsCommandHandler commandHandler;
    private final SmsGateway smsGateway;

    /**
     * Twilio webhook endpoint for incoming SMS messages.
     * Twilio sends form-urlencoded data with From, To, Body, etc.
     *
     * @param from the sender's phone number
     * @param to   the Twilio phone number that received the SMS
     * @param body the SMS message body
     * @return TwiML response (XML) for Twilio
     */
    @PostMapping(
            value = "/incoming",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE
    )
    public ResponseEntity<String> handleIncomingSms(
            @RequestParam("From") String from,
            @RequestParam("To") String to,
            @RequestParam("Body") String body) {
        
        log.info("📥 ┌─────────────────────────────────────────────────────────────");
        log.info("📥 │ SMS INBOUND");
        log.info("📥 │ From: {}", maskPhone(from));
        log.info("📥 │ To: {}", to);
        log.info("📥 │ Body: {}", truncateForLog(body));
        log.info("📥 └─────────────────────────────────────────────────────────────");

        try {
            SmsCommand command = smsParser.parse(from, body);
            log.info("📋 Parsed command: type={}, language={}", command.type(), command.detectedLanguage());
            
            String responseMessage = commandHandler.handle(command);
            log.info("📤 Response: {}", truncateForLog(responseMessage));
            
            // Use SmsGateway to generate TwiML response
            String twiml = smsGateway.generateTwimlResponse(responseMessage);
            
            return ResponseEntity.ok(twiml);
        } catch (Exception e) {
            log.error("❌ Error processing SMS from {}: {}", maskPhone(from), e.getMessage(), e);
            
            String errorMessage = "An error occurred. Please try again. / حدث خطأ. يرجى المحاولة مرة أخرى.";
            String errorTwiml = smsGateway.generateTwimlResponse(errorMessage);
            
            return ResponseEntity.ok(errorTwiml);
        }
    }

    /**
     * Simulation endpoint for development testing.
     * Allows testing SMS flow without actual Twilio integration.
     * Only available in dev profile.
     *
     * @param request the simulated SMS request
     * @return the response that would be sent
     */
    @PostMapping("/simulate")
    public ResponseEntity<SimulateResponse> simulateSms(@RequestBody SimulateRequest request) {
        log.info("🔬 ┌─────────────────────────────────────────────────────────────");
        log.info("🔬 │ SMS SIMULATION");
        log.info("🔬 │ From: {}", request.from());
        log.info("🔬 │ Body: {}", truncateForLog(request.body()));
        log.info("🔬 └─────────────────────────────────────────────────────────────");

        try {
            SmsCommand command = smsParser.parse(request.from(), request.body());
            String responseMessage = commandHandler.handle(command);
            
            log.info("🔬 Result: type={}, language={}", command.type(), command.detectedLanguage());
            log.info("🔬 Response: {}", truncateForLog(responseMessage));
            
            return ResponseEntity.ok(new SimulateResponse(
                    command.type().name(),
                    command.detectedLanguage().name(),
                    responseMessage,
                    true,
                    command.parameters()
            ));
        } catch (Exception e) {
            log.error("❌ Error simulating SMS: {}", e.getMessage(), e);
            return ResponseEntity.ok(new SimulateResponse(
                    "ERROR",
                    null,
                    "An error occurred: " + e.getMessage(),
                    false,
                    null
            ));
        }
    }

    /**
     * Health check endpoint for SMS service.
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        boolean gatewayAvailable = smsGateway.isAvailable();
        return ResponseEntity.ok(new HealthResponse(
                "SMS service is running",
                gatewayAvailable,
                gatewayAvailable ? "ready" : "mock_mode"
        ));
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, phone.length() - 4) + "****";
    }

    private String truncateForLog(String text) {
        if (text == null) return "null";
        if (text.length() <= 100) return text;
        return text.substring(0, 97) + "...";
    }

    /**
     * Request body for SMS simulation.
     */
    public record SimulateRequest(String from, String body) {}

    /**
     * Response body for SMS simulation.
     */
    public record SimulateResponse(
            String commandType,
            String detectedLanguage,
            String responseMessage,
            boolean success,
            Map<String, String> parsedParameters
    ) {}

    /**
     * Health check response.
     */
    public record HealthResponse(
            String message,
            boolean gatewayAvailable,
            String mode
    ) {}
}
