package com.safebirth.sms.simulator;

import com.safebirth.sms.handler.SmsCommandHandler;
import com.safebirth.sms.parser.SmsCommand;
import com.safebirth.sms.parser.SmsParser;
import com.safebirth.sms.simulator.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the SMS simulator.
 * Provides endpoints for simulating SMS conversations without actual Twilio integration.
 */
@RestController
@RequestMapping("/api/simulator")
@Tag(name = "SMS Simulator", description = "Endpoints for testing SMS functionality")
public class SmsSimulatorController {

    private static final Logger log = LoggerFactory.getLogger(SmsSimulatorController.class);

    private final SmsParser smsParser;
    private final SmsCommandHandler smsCommandHandler;
    private final ConversationService conversationService;

    public SmsSimulatorController(SmsParser smsParser, SmsCommandHandler smsCommandHandler,
                                   ConversationService conversationService) {
        this.smsParser = smsParser;
        this.smsCommandHandler = smsCommandHandler;
        this.conversationService = conversationService;
    }

    /**
     * Send a simulated SMS message and get the system response.
     */
    @PostMapping("/send")
    @Operation(summary = "Send simulated SMS", description = "Simulates sending an SMS and returns the system response")
    public ResponseEntity<SendMessageResponse> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        String phone = normalizePhone(request.phoneNumber());
        String body = request.body().trim();

        log.info("Simulator: Received message from {} - {}", phone, truncate(body));

        // Record the inbound message
        ChatMessage userMessage = conversationService.recordInbound(phone, body);

        // Parse and handle the command
        SmsCommand command = smsParser.parse(phone, body);
        String responseText = smsCommandHandler.handle(command);

        // Record the system response
        ChatMessage systemResponse = conversationService.recordOutbound(phone, responseText);

        log.info("Simulator: Response to {} - {}", phone, truncate(responseText));

        return ResponseEntity.ok(new SendMessageResponse(userMessage, systemResponse));
    }

    /**
     * Get the full conversation history for a phone number.
     */
    @GetMapping("/conversations/{phone}")
    @Operation(summary = "Get conversation", description = "Returns full conversation history for a phone number")
    public ResponseEntity<ConversationDto> getConversation(@PathVariable String phone) {
        String normalizedPhone = normalizePhone(phone);
        ConversationDto conversation = conversationService.getConversation(normalizedPhone);
        return ResponseEntity.ok(conversation);
    }

    /**
     * Get all known phone devices.
     */
    @GetMapping("/devices")
    @Operation(summary = "List devices", description = "Returns all known phone numbers with metadata")
    public ResponseEntity<List<DeviceDto>> getDevices() {
        List<DeviceDto> devices = conversationService.getDevices();
        return ResponseEntity.ok(devices);
    }

    /**
     * Get all outbound messages from the system.
     */
    @GetMapping("/outbox")
    @Operation(summary = "Get outbox", description = "Returns all outbound messages sent by the system")
    public ResponseEntity<List<ChatMessage>> getOutbox() {
        List<ChatMessage> outbox = conversationService.getOutbox();
        return ResponseEntity.ok(outbox);
    }

    /**
     * Reset the simulator - clear all conversations and outbox.
     */
    @DeleteMapping("/reset")
    @Operation(summary = "Reset simulator", description = "Clears all conversations and the outbox")
    public ResponseEntity<Map<String, String>> reset() {
        conversationService.reset();
        return ResponseEntity.ok(Map.of("status", "reset", "message", "All conversations and outbox cleared"));
    }

    /**
     * Normalize phone number to a consistent format.
     */
    private String normalizePhone(String phone) {
        if (phone == null) return "";

        // Remove spaces and dashes
        String normalized = phone.replaceAll("[\\s-]", "");

        // Ensure it starts with + if it has a country code
        if (normalized.matches("^\\d{10,}$") && !normalized.startsWith("+")) {
            // Assume Sudan country code if not present
            normalized = "+249" + (normalized.startsWith("0") ? normalized.substring(1) : normalized);
        }

        return normalized;
    }

    private String truncate(String text) {
        if (text == null) return "null";
        if (text.length() <= 50) return text;
        return text.substring(0, 47) + "...";
    }
}
