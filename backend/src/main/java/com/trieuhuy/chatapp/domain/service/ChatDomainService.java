package com.trieuhuy.chatapp.domain.service;

import com.trieuhuy.chatapp.domain.model.Conversation;
import com.trieuhuy.chatapp.domain.model.Message;
import com.trieuhuy.chatapp.domain.repository.ConversationRepository;
import com.trieuhuy.chatapp.domain.repository.MessageRepository;
import com.trieuhuy.chatapp.domain.repository.UserRepository;
import com.trieuhuy.chatapp.infrastructure.persistence.entity.ConversationEntity;
import com.trieuhuy.chatapp.infrastructure.persistence.entity.ConversationMemberEntity;
import com.trieuhuy.chatapp.infrastructure.persistence.entity.ConversationMemberId;
import com.trieuhuy.chatapp.infrastructure.persistence.entity.UserEntity;
import com.trieuhuy.chatapp.infrastructure.persistence.mapper.UserMapper;
import com.trieuhuy.chatapp.infrastructure.persistence.repository.ConversationJpaRepository;
import com.trieuhuy.chatapp.infrastructure.persistence.repository.ConversationMemberJpaRepository;
import com.trieuhuy.chatapp.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatDomainService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ConversationJpaRepository conversationJpaRepository;
    private final ConversationMemberJpaRepository memberJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    @Transactional
    public Conversation getOrCreatePrivateConversation(UUID userId1, UUID userId2) {
        return conversationRepository.findPrivateConversationBetween(userId1, userId2)
                .orElseGet(() -> {
                    ConversationEntity conversationEntity = new ConversationEntity();
                    conversationEntity.setType("PRIVATE");
                    conversationEntity.setName(null);
                    conversationEntity.setDeleted(false);
                    
                    UserEntity createdBy = userJpaRepository.findById(userId1)
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId1));
                    conversationEntity.setCreatedBy(createdBy);
                    
                    ConversationEntity savedEntity = conversationJpaRepository.save(conversationEntity);
                    
                    UserEntity user1 = userJpaRepository.findById(userId1)
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId1));
                    UserEntity user2 = userJpaRepository.findById(userId2)
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId2));
                    
                    ConversationMemberEntity member1 = new ConversationMemberEntity();
                    member1.setId(new ConversationMemberId(savedEntity.getId(), userId1));
                    member1.setConversation(savedEntity);
                    member1.setUser(user1);
                    member1.setRole("MEMBER");
                    member1.setMuted(false);
                    memberJpaRepository.save(member1);
                    
                    ConversationMemberEntity member2 = new ConversationMemberEntity();
                    member2.setId(new ConversationMemberId(savedEntity.getId(), userId2));
                    member2.setConversation(savedEntity);
                    member2.setUser(user2);
                    member2.setRole("MEMBER");
                    member2.setMuted(false);
                    memberJpaRepository.save(member2);
                    
                    return conversationRepository.findById(savedEntity.getId())
                            .orElseThrow(() -> new IllegalStateException("Failed to create conversation"));
                });
    }

    @Transactional(readOnly = true)
    public List<Conversation> getUserConversations(UUID userId) {
        return conversationRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Conversation getConversation(UUID conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + conversationId));
    }

    @Transactional
    public Message sendMessage(UUID conversationId, UUID senderId, String content, String type) {
        Conversation conversation = getConversation(conversationId);
        
        if (!conversation.getMemberIds().contains(senderId)) {
            throw new IllegalArgumentException("User is not a member of this conversation");
        }

        Message message = Message.builder()
                .conversationId(conversationId)
                .senderId(senderId)
                .type(type != null ? type : "TEXT")
                .content(content)
                .isDeleted(false)
                .build();

        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<Message> getMessages(UUID conversationId, int limit, UUID beforeMessageId) {
        return messageRepository.findByConversationId(conversationId, limit, beforeMessageId);
    }

    @Transactional(readOnly = true)
    public List<Message> getNewMessages(UUID conversationId, UUID afterMessageId) {
        return messageRepository.findByConversationIdAndAfter(conversationId, afterMessageId);
    }
}

