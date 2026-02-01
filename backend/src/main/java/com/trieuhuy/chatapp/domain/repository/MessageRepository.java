package com.trieuhuy.chatapp.domain.repository;

import com.trieuhuy.chatapp.domain.model.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository {
    Optional<Message> findById(UUID id);
    
    List<Message> findByConversationId(UUID conversationId, int limit, UUID beforeMessageId);
    
    List<Message> findByConversationIdAndAfter(UUID conversationId, UUID afterMessageId);
    
    Message save(Message message);
    
    void delete(Message message);
    
    long countByConversationId(UUID conversationId);
}

