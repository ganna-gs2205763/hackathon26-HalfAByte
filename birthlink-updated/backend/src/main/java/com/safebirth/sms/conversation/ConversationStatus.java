package com.safebirth.sms.conversation;

/**
 * Status of a conversation.
 */
public enum ConversationStatus {
    /**
     * Conversation is ongoing.
     */
    ACTIVE,

    /**
     * Conversation completed successfully.
     */
    COMPLETED,

    /**
     * Conversation expired due to timeout.
     */
    EXPIRED
}
