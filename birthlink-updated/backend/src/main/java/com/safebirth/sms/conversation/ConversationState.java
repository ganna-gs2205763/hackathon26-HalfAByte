package com.safebirth.sms.conversation;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity for tracking multi-turn conversation state.
 * Persists conversation context between SMS messages.
 */
@Entity
@Table(name = "conversation_states", indexes = {
        @Index(name = "idx_conversation_phone_status", columnList = "phoneNumber, status")
})
public class ConversationState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Phone number for this conversation.
     */
    @Column(nullable = false, length = 20)
    private String phoneNumber;

    /**
     * Type of conversation (registration, help request, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConversationType conversationType;

    /**
     * Data collected so far (JSON format).
     * Example: {"age": 25, "camp": "Amman"}
     */
    @Column(columnDefinition = "TEXT")
    private String collectedData;

    /**
     * Message history for context (JSON array).
     * Example: [{"role": "user", "content": "hi"}, {"role": "assistant", "content":
     * "Hello!"}]
     */
    @Column(columnDefinition = "TEXT")
    private String messageHistory;

    /**
     * Number of conversation turns.
     */
    @Column
    private int turnCount = 0;

    /**
     * Detected/preferred language.
     */
    @Column(length = 10)
    private String language;

    /**
     * Current status.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ConversationStatus status = ConversationStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public ConversationState() {
    }

    public ConversationState(Long id, String phoneNumber, ConversationType conversationType, String collectedData,
                             String messageHistory, int turnCount, String language, ConversationStatus status,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.conversationType = conversationType;
        this.collectedData = collectedData;
        this.messageHistory = messageHistory;
        this.turnCount = turnCount;
        this.language = language;
        this.status = status != null ? status : ConversationStatus.ACTIVE;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public ConversationType getConversationType() {
        return conversationType;
    }

    public String getCollectedData() {
        return collectedData;
    }

    public String getMessageHistory() {
        return messageHistory;
    }

    public int getTurnCount() {
        return turnCount;
    }

    public String getLanguage() {
        return language;
    }

    public ConversationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setConversationType(ConversationType conversationType) {
        this.conversationType = conversationType;
    }

    public void setCollectedData(String collectedData) {
        this.collectedData = collectedData;
    }

    public void setMessageHistory(String messageHistory) {
        this.messageHistory = messageHistory;
    }

    public void setTurnCount(int turnCount) {
        this.turnCount = turnCount;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setStatus(ConversationStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Increment turn count.
     */
    public void incrementTurn() {
        this.turnCount++;
    }

    public static ConversationStateBuilder builder() {
        return new ConversationStateBuilder();
    }

    public static class ConversationStateBuilder {
        private Long id;
        private String phoneNumber;
        private ConversationType conversationType;
        private String collectedData;
        private String messageHistory;
        private int turnCount = 0;
        private String language;
        private ConversationStatus status = ConversationStatus.ACTIVE;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public ConversationStateBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ConversationStateBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public ConversationStateBuilder conversationType(ConversationType conversationType) {
            this.conversationType = conversationType;
            return this;
        }

        public ConversationStateBuilder collectedData(String collectedData) {
            this.collectedData = collectedData;
            return this;
        }

        public ConversationStateBuilder messageHistory(String messageHistory) {
            this.messageHistory = messageHistory;
            return this;
        }

        public ConversationStateBuilder turnCount(int turnCount) {
            this.turnCount = turnCount;
            return this;
        }

        public ConversationStateBuilder language(String language) {
            this.language = language;
            return this;
        }

        public ConversationStateBuilder status(ConversationStatus status) {
            this.status = status;
            return this;
        }

        public ConversationStateBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ConversationStateBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ConversationState build() {
            return new ConversationState(id, phoneNumber, conversationType, collectedData, messageHistory,
                    turnCount, language, status, createdAt, updatedAt);
        }
    }
}
