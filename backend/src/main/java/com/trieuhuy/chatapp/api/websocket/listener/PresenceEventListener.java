package com.trieuhuy.chatapp.api.websocket.listener;

import java.security.Principal;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.trieuhuy.chatapp.application.service.PresenceApplicationService;
import com.trieuhuy.chatapp.infrastructure.redis.presence.RedisPresenceStore;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Slf4j
public class PresenceEventListener {

    private final PresenceApplicationService presenceService;
    private final RedisPresenceStore redisPresenceStore;
    
    @EventListener
    public void onConnect(SessionConnectEvent event) {
        Principal principal = event.getUser();
        if (principal != null) {
            try {
                UUID userId = UUID.fromString(principal.getName());
                boolean isFirstConnection = redisPresenceStore.connect(userId);
                if (isFirstConnection) {
                    presenceService.userConnected(userId);
                }
                log.debug("User connected: {}", userId);
            } catch (Exception e) {
                log.error("Error handling connect event", e);
            }
        } else {
            log.warn("onConnect: Cannot get event user");
        }
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal != null) {
            try {
                UUID userId = UUID.fromString(principal.getName());
                boolean isLastConnection = redisPresenceStore.disconnect(userId);
                if (isLastConnection) {
                    presenceService.userDisconnected(userId);
                }
                log.debug("User disconnected: {}", userId);
            } catch (Exception e) {
                log.error("Error handling disconnect event", e);
            }
        } else {
            log.warn("onDisconnect: Cannot get event user");
        }
    }
}
