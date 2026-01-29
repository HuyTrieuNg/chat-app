package com.trieuhuy.chatapp.api.controller;

import com.trieuhuy.chatapp.application.dto.ConversationDto;
import com.trieuhuy.chatapp.application.dto.MessageDto;
import com.trieuhuy.chatapp.application.service.ChatApplicationService;
import com.trieuhuy.chatapp.infrastructure.security.util.SecurityUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ChatController {

    private final ChatApplicationService chatService;

    @GetMapping("/conversations")
    public ResponseEntity<@NonNull List<ConversationDto>> getConversations() {
        UUID userId = SecurityUtils.getCurrentUserId();
        log.debug("Getting conversations for user: {}", userId);
        List<ConversationDto> conversations = chatService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<@NonNull ConversationDto> getConversation(@PathVariable UUID conversationId) {
        log.debug("Getting conversation: {}", conversationId);
        ConversationDto conversation = chatService.getConversation(conversationId);
        return ResponseEntity.ok(conversation);
    }

    @PostMapping("/conversations/private/{otherUserId}")
    public ResponseEntity<@NonNull ConversationDto> getOrCreatePrivateConversation(@PathVariable UUID otherUserId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        log.debug("Getting or creating private conversation between {} and {}", currentUserId, otherUserId);
        ConversationDto conversation = chatService.getOrCreatePrivateConversation(currentUserId, otherUserId);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<@NonNull List<MessageDto>> getMessages(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) UUID before) {
        log.debug("Getting messages for conversation {} with limit {} and before {}", 
                conversationId, limit, before);
        List<MessageDto> messages = 
                chatService.getMessages(conversationId, limit, before);
        return ResponseEntity.ok(messages);
    }
}

