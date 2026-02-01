package com.trieuhuy.chatapp.api.websocket.controller;

import com.trieuhuy.chatapp.application.dto.MessageDto;
import com.trieuhuy.chatapp.application.dto.SendMessageRequest;
import com.trieuhuy.chatapp.application.service.ChatApplicationService;
import com.trieuhuy.chatapp.api.websocket.publisher.MessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageWebSocketController {

    private final ChatApplicationService chatService;
    private final MessagePublisher messagePublisher;

    @MessageMapping("/chat/send")
    public void sendMessage(@Payload SendMessageRequest request, StompHeaderAccessor headerAccessor) {
        try {
            if (headerAccessor.getUser() == null) {
                log.warn("Received message from unauthenticated user");
                return;
            }

            UUID senderId = UUID.fromString(headerAccessor.getUser().getName());
            log.debug("Received message from user {} to conversation {}", senderId, request.conversationId());

            // Save message to database
            MessageDto message = chatService.sendMessage(senderId, request);

            // Publish to WebSocket subscribers
            messagePublisher.publishMessage(request.conversationId(), message);
            
        } catch (Exception e) {
            log.error("Error handling message", e);
        }
    }
}


