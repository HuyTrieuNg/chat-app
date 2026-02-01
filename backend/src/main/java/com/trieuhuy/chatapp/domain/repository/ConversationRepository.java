package com.trieuhuy.chatapp.domain.repository;

import com.trieuhuy.chatapp.domain.model.Conversation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository {
    Optional<Conversation> findById(UUID id);
    
    Optional<Conversation> findPrivateConversationBetween(UUID userId1, UUID userId2);
    
    List<Conversation> findByUserId(UUID userId);
    
    Conversation save(Conversation conversation);
    
    void delete(Conversation conversation);
    
    boolean existsById(UUID id);
}

