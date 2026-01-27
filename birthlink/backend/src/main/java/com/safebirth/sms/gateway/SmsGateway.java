package com.safebirth.sms.gateway;

/**
 * Interface for SMS gateway operations.
 * Implementations include Twilio (production) and Mock (development).
 */
public interface SmsGateway {

    /**
     * Send an SMS message to the specified phone number.
     *
     * @param to      the recipient phone number (E.164 format)
     * @param message the message body
     * @return the message SID or identifier
     * @throws com.safebirth.exception.SmsDeliveryException if sending fails
     */
    String sendSms(String to, String message);

    /**
     * Generate a TwiML XML response for Twilio webhook.
     * This is the format Twilio expects as a response to incoming SMS.
     *
     * @param message the message to send back to the user
     * @return TwiML XML string
     */
    String generateTwimlResponse(String message);

    /**
     * Check if the gateway is available and properly configured.
     *
     * @return true if the gateway can send messages
     */
    boolean isAvailable();
}
