package com.trieuhuy.chatapp.application.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.trieuhuy.chatapp.api.websocket.publisher.PresencePublisher;
import com.trieuhuy.chatapp.domain.model.UserStatus;
import com.trieuhuy.chatapp.domain.repository.CacheRepository;
import com.trieuhuy.chatapp.domain.service.UserDomainService;
import com.trieuhuy.chatapp.infrastructure.redis.presence.PresenceDebounce;
import com.trieuhuy.chatapp.infrastructure.redis.presence.RedisPresenceStore;
import com.trieuhuy.chatapp.infrastructure.redis.util.RedisKeyBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Redis store is the source of truth for real-time presence status
// Database is updated with debounce to reduce write load
@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceApplicationService {

    private final UserDomainService userDomainService;
    private final PresencePublisher presencePublisher;
    private final PresenceDebounce presenceDebounce;
    private final CacheRepository cacheRepository;
    private final RedisPresenceStore redisPresenceStore;

    public void userConnected(UUID userId) {
        redisPresenceStore.setStatus(userId, UserStatus.ONLINE);
        log.debug("Redis: Set user {} status to ONLINE", userId);
        
        if (presenceDebounce.shouldUpdateDb(userId)) {
            userDomainService.setUserOnline(userId);
            log.debug("DB: Updated user {} to ONLINE", userId);
        }
        
        presencePublisher.broadcastUserStatusChange(userId, UserStatus.ONLINE);
        log.debug("WebSocket: Broadcasted user {} ONLINE", userId);
    }

    public void userDisconnected(UUID userId) {
        redisPresenceStore.setStatus(userId, UserStatus.OFFLINE);
        log.debug("Redis: Set user {} status to OFFLINE", userId);
        
        if (presenceDebounce.shouldUpdateDb(userId)) {
            userDomainService.setUserOffline(userId);
            evictUserCache(userId);
            log.debug("DB: Updated user {} to OFFLINE with lastSeen timestamp", userId);
        }
        
        var user = userDomainService.findById(userId);
        presencePublisher.broadcastUserStatusChange(userId, UserStatus.OFFLINE, user.getLastSeenAt());
        log.debug("WebSocket: Broadcasted user {} OFFLINE with lastSeen={}", userId, user.getLastSeenAt());
    }

    public void userAway(UUID userId) {
        redisPresenceStore.setStatus(userId, UserStatus.AWAY);
        log.debug("Redis: Set user {} status to AWAY", userId);
        
        userDomainService.setUserAway(userId);
        evictUserCache(userId);
        log.debug("DB: Updated user {} to AWAY", userId);
        
        presencePublisher.broadcastUserStatusChange(userId, UserStatus.AWAY);
        log.debug("WebSocket: Broadcasted user {} AWAY", userId);
    }

    public void updateUserActivity(UUID userId) {
        redisPresenceStore.updateLastActivity(userId);
        log.trace("Updated activity for user: {}", userId);
        
        String currentStatus = redisPresenceStore.getStatus(userId);
        if ("AWAY".equals(currentStatus)) {
            redisPresenceStore.setStatus(userId, UserStatus.ONLINE);
            presencePublisher.broadcastUserStatusChange(userId, UserStatus.ONLINE);
            log.debug("User {} returned from AWAY to ONLINE", userId);
        }
    }

    private void evictUserCache(UUID userId) {
        cacheRepository.evict(RedisKeyBuilder.userProfile(userId));
    }

    public Map<UUID, UserStatus> getBulkPresenceStatus(List<UUID> userIds) {
        log.debug("Getting bulk presence status for {} users", userIds.size());
        return userIds.stream()
                .collect(Collectors.toMap(
                        userId -> userId,
                        userId -> {
                            String status = redisPresenceStore.getStatus(userId);
                            try {
                                return UserStatus.valueOf(status);
                            } catch (IllegalArgumentException e) {
                                log.warn("Invalid status '{}' for user {}, defaulting to OFFLINE", status, userId);
                                return UserStatus.OFFLINE;
                            }
                        }
                ));
    }

    public UserStatus getUserPresenceStatus(UUID userId) {
        String status = redisPresenceStore.getStatus(userId);
        try {
            return UserStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status '{}' for user {}, defaulting to OFFLINE", status, userId);
            return UserStatus.OFFLINE;
        }
    }
}
