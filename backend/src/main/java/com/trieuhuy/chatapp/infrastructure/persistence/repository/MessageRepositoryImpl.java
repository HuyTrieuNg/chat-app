package com.trieuhuy.chatapp.infrastructure.persistence.repository;

import com.trieuhuy.chatapp.domain.model.Message;
import com.trieuhuy.chatapp.domain.repository.MessageRepository;
import com.trieuhuy.chatapp.infrastructure.persistence.entity.ConversationEntity;
import com.trieuhuy.chatapp.infrastructure.persistence.entity.MessageEntity;
import com.trieuhuy.chatapp.infrastructure.persistence.entity.UserEntity;
import com.trieuhuy.chatapp.infrastructure.persistence.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Primary
@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepository {

    private final MessageJpaRepository jpaRepository;
    private final ConversationJpaRepository conversationJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final MessageMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Message> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findByConversationId(UUID conversationId, int limit, UUID beforeMessageId) {
        Pageable pageable = PageRequest.of(0, limit);
        List<MessageEntity> entities = jpaRepository.findByConversationIdWithPagination(
                conversationId, beforeMessageId, pageable);
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findByConversationIdAndAfter(UUID conversationId, UUID afterMessageId) {
        List<MessageEntity> entities = jpaRepository.findByConversationIdAndAfter(
                conversationId, afterMessageId);
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Message save(Message message) {
        MessageEntity entity = mapper.toEntity(message);
        
        if (message.getConversationId() != null) {
            ConversationEntity conversation = conversationJpaRepository.findById(message.getConversationId())
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + message.getConversationId()));
            entity.setConversation(conversation);
        }
        
        if (message.getSenderId() != null) {
            UserEntity sender = userJpaRepository.findById(message.getSenderId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + message.getSenderId()));
            entity.setSender(sender);
        }
        
        MessageEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional
    public void delete(Message message) {
        MessageEntity entity = mapper.toEntity(message);
        entity.setDeleted(true);
        jpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByConversationId(UUID conversationId) {
        return jpaRepository.countByConversationIdAndIsDeletedFalse(conversationId);
    }
}

