package com.trieuhuy.chatapp.infrastructure.scheduler;

import java.util.Set;
import java.util.UUID;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trieuhuy.chatapp.application.service.PresenceApplicationService;
import com.trieuhuy.chatapp.infrastructure.redis.presence.RedisPresenceStore;

import lombok.RequiredArgsConstructor;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class PresenceScheduler {
    
    private final RedisPresenceStore store;
    private final PresenceApplicationService service;
    private final Set<UUID> onlineUsers;

    @Scheduled(fixedRate = 60_000) // run every second
    public void detectAway() {
        for (var userId : onlineUsers) {
            if (store.shouldAway(userId)) {
                service.userAway(userId);
            }
        }
    }
}
