package com.trieuhuy.chatapp.api.websocket.publisher;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisWebSocketPublisher {

    private final SimpMessagingTemplate template;

    public void sendToUser(String userId, String destination, Object payload) {
        template.convertAndSendToUser(userId, destination, payload);
    }
}