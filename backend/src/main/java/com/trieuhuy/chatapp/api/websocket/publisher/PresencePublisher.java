package com.trieuhuy.chatapp.api.websocket.publisher;

import java.time.Instant;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.trieuhuy.chatapp.application.dto.PresenceStatusDto;
import com.trieuhuy.chatapp.domain.model.UserStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PresencePublisher {
    
    private final SimpMessagingTemplate template;

    public void broadcastUserStatusChange(UUID userId, UserStatus newStatus) {
        broadcastUserStatusChange(userId, newStatus, null);
    }

    public void broadcastUserStatusChange(UUID userId, UserStatus newStatus, Instant lastSeen) {
        String destination = "/topic/presence";
        PresenceStatusDto payload = new PresenceStatusDto(userId, newStatus, lastSeen);
        log.debug("Broadcasting presence: userId={}, status={}, lastSeen={} to {}", 
                  userId, newStatus, lastSeen, destination);
        template.convertAndSend(destination, payload);
    }
}
