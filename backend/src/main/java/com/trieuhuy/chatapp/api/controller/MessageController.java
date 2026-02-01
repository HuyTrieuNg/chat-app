package com.trieuhuy.chatapp.api.controller;

import com.trieuhuy.chatapp.application.dto.MessageDto;
import com.trieuhuy.chatapp.application.dto.SendMessageRequest;
import com.trieuhuy.chatapp.application.service.ChatApplicationService;
import com.trieuhuy.chatapp.infrastructure.security.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MessageController {

    private final ChatApplicationService chatService;

    @PostMapping
    public ResponseEntity<@NonNull MessageDto> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        UUID senderId = SecurityUtils.getCurrentUserId();
        log.debug("Sending message from {} to conversation {}", senderId, request.conversationId());
        MessageDto message = chatService.sendMessage(senderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @GetMapping("/conversations/{conversationId}/new")
    public ResponseEntity<@NonNull List<MessageDto>> getNewMessages(
            @PathVariable UUID conversationId,
            @RequestParam UUID after) {
        log.debug("Getting new messages for conversation {} after {}", conversationId, after);
        List<MessageDto> messages = chatService.getNewMessages(conversationId, after);
        return ResponseEntity.ok(messages);
    }
}

