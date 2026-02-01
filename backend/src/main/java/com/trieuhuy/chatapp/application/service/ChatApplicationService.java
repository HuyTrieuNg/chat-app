package com.trieuhuy.chatapp.application.service;

import com.trieuhuy.chatapp.application.dto.ConversationDto;
import com.trieuhuy.chatapp.application.dto.MessageDto;
import com.trieuhuy.chatapp.application.dto.SendMessageRequest;
import com.trieuhuy.chatapp.domain.model.Conversation;
import com.trieuhuy.chatapp.domain.model.Message;
import com.trieuhuy.chatapp.domain.model.User;
import com.trieuhuy.chatapp.domain.repository.UserRepository;
import com.trieuhuy.chatapp.domain.service.ChatDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatApplicationService {

    private final ChatDomainService chatDomainService;
    private final UserRepository userRepository;

    @Transactional
    public ConversationDto getOrCreatePrivateConversation(UUID currentUserId, UUID otherUserId) {
        log.debug("Getting or creating private conversation between {} and {}", currentUserId, otherUserId);
        
        userRepository.findById(otherUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + otherUserId));

        Conversation conversation = chatDomainService.getOrCreatePrivateConversation(currentUserId, otherUserId);
        return ConversationDto.from(conversation);
    }

    @Transactional(readOnly = true)
    public List<ConversationDto> getUserConversations(UUID userId) {
        log.debug("Getting conversations for user: {}", userId);
        List<Conversation> conversations = chatDomainService.getUserConversations(userId);
        return conversations.stream()
                .map(ConversationDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConversationDto getConversation(UUID conversationId) {
        log.debug("Getting conversation: {}", conversationId);
        Conversation conversation = chatDomainService.getConversation(conversationId);
        return ConversationDto.from(conversation);
    }

    @Transactional
    public MessageDto sendMessage(UUID senderId, SendMessageRequest request) {
        log.debug("Sending message from {} to conversation {}", senderId, request.conversationId());
        
        Message message = chatDomainService.sendMessage(
                request.conversationId(),
                senderId,
                request.content(),
                request.getType()
        );

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + senderId));

        return MessageDto.from(message, sender.getUsername());
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getMessages(UUID conversationId, int limit, UUID beforeMessageId) {
        log.debug("Getting messages for conversation {} with limit {} and before {}", 
                conversationId, limit, beforeMessageId);
        
        List<Message> messages = chatDomainService.getMessages(conversationId, limit, beforeMessageId);
        
        return messages.stream()
                .map(msg -> {
                    User sender = userRepository.findById(msg.getSenderId())
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + msg.getSenderId()));
                    return MessageDto.from(msg, sender.getUsername());
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getNewMessages(UUID conversationId, UUID afterMessageId) {
        log.debug("Getting new messages for conversation {} after {}", conversationId, afterMessageId);
        
        List<Message> messages = chatDomainService.getNewMessages(conversationId, afterMessageId);
        
        return messages.stream()
                .map(msg -> {
                    User sender = userRepository.findById(msg.getSenderId())
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + msg.getSenderId()));
                    return MessageDto.from(msg, sender.getUsername());
                })
                .collect(Collectors.toList());
    }
}

