package com.trieuhuy.chatapp.infrastructure.scheduler;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trieuhuy.chatapp.application.service.PresenceApplicationService;
import com.trieuhuy.chatapp.infrastructure.redis.presence.RedisPresenceStore;
import com.trieuhuy.chatapp.infrastructure.redis.util.RedisKeyBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler for checking user presence status based on activity and heartbeat.
 * Runs periodically to update user status (AWAY/OFFLINE) based on inactivity.
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class PresenceScheduler {
    
    private final RedisPresenceStore redisPresenceStore;
    private final PresenceApplicationService presenceService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Check online users for AWAY status every 60 seconds.
     * If lastActivity exceeds threshold, mark user as AWAY.
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    public void checkAwayStatus() {
        try {
            String onlineUsersKey = RedisKeyBuilder.presenceOnlineUsers();
            Set<Object> onlineUsers = redisTemplate.opsForSet().members(onlineUsersKey);
            
            if (onlineUsers == null || onlineUsers.isEmpty()) {
                return;
            }

            log.debug("Checking AWAY status for {} online users", onlineUsers.size());
            
            for (Object userIdObj : onlineUsers) {
                try {
                    UUID userId = UUID.fromString(userIdObj.toString());
                    
                    // Only check users with ONLINE status
                    String currentStatus = redisPresenceStore.getStatus(userId);
                    if (!"ONLINE".equals(currentStatus)) {
                        continue;
                    }
                    
                    // Check if user should be marked as away
                    if (redisPresenceStore.shouldAway(userId)) {
                        log.info("User {} is inactive, marking as AWAY", userId);
                        presenceService.userAway(userId);
                    }
                } catch (Exception e) {
                    log.error("Error checking away status for user: {}", userIdObj, e);
                }
            }
        } catch (Exception e) {
            log.error("Error in checkAwayStatus scheduler", e);
        }
    }

    /**
     * Check online users for OFFLINE status every 30 seconds.
     * If heartbeat has timed out AND sockets are empty, mark user as OFFLINE.
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 30000)
    public void checkOfflineStatus() {
        try {
            String onlineUsersKey = RedisKeyBuilder.presenceOnlineUsers();
            Set<Object> onlineUsers = redisTemplate.opsForSet().members(onlineUsersKey);
            
            if (onlineUsers == null || onlineUsers.isEmpty()) {
                return;
            }

            log.debug("Checking OFFLINE status for {} online users", onlineUsers.size());
            
            for (Object userIdObj : onlineUsers) {
                try {
                    UUID userId = UUID.fromString(userIdObj.toString());
                    
                    // Check if user should be marked as offline
                    // (heartbeat timeout AND no active sockets)
                    if (redisPresenceStore.shouldOffline(userId)) {
                        log.info("User {} heartbeat timed out with no active sockets, marking as OFFLINE", userId);
                        presenceService.userDisconnected(userId);
                    }
                } catch (Exception e) {
                    log.error("Error checking offline status for user: {}", userIdObj, e);
                }
            }
        } catch (Exception e) {
            log.error("Error in checkOfflineStatus scheduler", e);
        }
    }
}
