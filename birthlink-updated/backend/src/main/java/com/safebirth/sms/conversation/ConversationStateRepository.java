package com.safebirth.sms.conversation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for conversation state persistence.
 */
@Repository
public interface ConversationStateRepository extends JpaRepository<ConversationState, Long> {

    /**
     * Find active conversation for a phone number.
     */
    Optional<ConversationState> findByPhoneNumberAndStatus(String phoneNumber, ConversationStatus status);

    /**
     * Find all active conversations for a phone number.
     */
    List<ConversationState> findAllByPhoneNumberAndStatus(String phoneNumber, ConversationStatus status);

    /**
     * Find expired conversations (for cleanup).
     */
    @Query("SELECT c FROM ConversationState c WHERE c.status = 'ACTIVE' AND c.updatedAt < :cutoff")
    List<ConversationState> findExpiredConversations(LocalDateTime cutoff);
}
