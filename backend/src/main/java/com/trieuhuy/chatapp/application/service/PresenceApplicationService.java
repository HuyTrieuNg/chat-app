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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceApplicationService {

    private static final String USER_PROFILE_KEY_PREFIX = "user:profile:";

    private final UserDomainService userDomainService;
    private final PresencePublisher presencePublisher;
    private final PresenceDebounce presenceDebounce;
    private final CacheRepository cacheRepository;
    private final RedisPresenceStore redisPresenceStore;

    public void userConnected(UUID userId) {
        if (presenceDebounce.shouldUpdateDb(userId)) {
            userDomainService.setUserOnline(userId);
            log.debug("update user {} last seen", userId);
        }
        presencePublisher.broadcastUserStatusChange(userId, UserStatus.ONLINE);
        log.debug("broadcast user {} online", userId);
    }

    public void userDisconnected(UUID userId) {
        if (presenceDebounce.shouldUpdateDb(userId)) {
            userDomainService.setUserOffline(userId);
            evictUserCache(userId);
            log.debug("update user {} last seen", userId);
        }
        presencePublisher.broadcastUserStatusChange(userId, UserStatus.OFFLINE);
        log.debug("broadcast user {} offline", userId);
    }

    public void userAway(UUID userId) {
        userDomainService.setUserAway(userId);
        evictUserCache(userId);
        presencePublisher.broadcastUserStatusChange(userId, UserStatus.AWAY);
        log.debug("broadcast user {} away", userId);
    }

    private void evictUserCache(UUID userId) {
        cacheRepository.evict(USER_PROFILE_KEY_PREFIX + userId);
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
