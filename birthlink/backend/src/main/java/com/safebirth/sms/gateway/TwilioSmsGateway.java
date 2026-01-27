package com.safebirth.sms.gateway;

import com.safebirth.config.TwilioConfig;
import com.safebirth.exception.SmsDeliveryException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.messaging.Body;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Production SMS gateway implementation using Twilio API.
 * Only activated when twilio.mock-enabled is false.
 * 
 * Uses Twilio SDK for:
 * - Sending outbound SMS via Message.creator()
 * - Generating TwiML responses via MessagingResponse builder
 */
@Component
@ConditionalOnProperty(name = "twilio.mock-enabled", havingValue = "false")
@RequiredArgsConstructor
@Slf4j
public class TwilioSmsGateway implements SmsGateway {

    private final TwilioConfig twilioConfig;

    @Override
    public String sendSms(String to, String message) {
        try {
            log.info("📤 Sending SMS to {} via Twilio", maskPhoneNumber(to));
            
            Message twilioMessage = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(twilioConfig.getPhoneNumber()),
                    message
            ).create();

            String sid = twilioMessage.getSid();
            log.info("✅ SMS sent successfully. SID: {}", sid);
            
            return sid;
        } catch (Exception e) {
            log.error("❌ Failed to send SMS to {}: {}", maskPhoneNumber(to), e.getMessage());
            throw new SmsDeliveryException("Failed to send SMS: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateTwimlResponse(String message) {
        try {
            // Use Twilio TwiML SDK to generate proper XML response
            MessagingResponse response = new MessagingResponse.Builder()
                    .message(new com.twilio.twiml.messaging.Message.Builder()
                            .body(new Body.Builder(message).build())
                            .build())
                    .build();
            
            String twiml = response.toXml();
            log.debug("📨 Generated TwiML response: {}", truncateForLog(message));
            return twiml;
        } catch (TwiMLException e) {
            log.error("Failed to generate TwiML: {}", e.getMessage());
            // Fallback to manual XML generation
            return String.format("""
                    <?xml version="1.0" encoding="UTF-8"?>
                    <Response>
                        <Message>%s</Message>
                    </Response>
                    """, escapeXml(message));
        }
    }

    @Override
    public boolean isAvailable() {
        return twilioConfig.getAccountSid() != null 
                && !twilioConfig.getAccountSid().isEmpty()
                && !twilioConfig.getAccountSid().equals("your_account_sid");
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "***";
        }
        return phoneNumber.substring(0, phoneNumber.length() - 4) + "****";
    }

    private String truncateForLog(String text) {
        if (text == null) return "null";
        if (text.length() <= 100) return text;
        return text.substring(0, 97) + "...";
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
