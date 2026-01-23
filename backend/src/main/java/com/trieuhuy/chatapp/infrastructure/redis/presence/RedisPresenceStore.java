package com.trieuhuy.chatapp.infrastructure.redis.presence;

import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.trieuhuy.chatapp.domain.model.UserStatus;
import com.trieuhuy.chatapp.infrastructure.redis.util.RedisKeyBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisPresenceStore {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final long AWAY_THRESHOLD_MS = 5 * 60 * 1000; // 5 minutes
    private static final long HEARTBEAT_TIMEOUT_MS = 60 * 1000; // 60 seconds

    public boolean addSocket(UUID userId, String sessionId) {
        String socketsKey = RedisKeyBuilder.presenceSockets(userId);
        
        // Add session to user's active sockets set
        redisTemplate.opsForSet().add(socketsKey, sessionId);
        
        // Initialize heartbeat for this user
        updateHeartbeat(userId);
        updateLastActivity(userId);
        
        Long socketsCount = redisTemplate.opsForSet().size(socketsKey);
        log.debug("Added socket {} for user {}, total sockets: {}", sessionId, userId, socketsCount);
        
        // Return true if this was the first socket
        return socketsCount != null && socketsCount == 1;
    }

    public boolean removeSocket(UUID userId, String sessionId) {
        String socketsKey = RedisKeyBuilder.presenceSockets(userId);
        
        // Remove session from set
        redisTemplate.opsForSet().remove(socketsKey, sessionId);
        
        // Update last activity
        updateLastActivity(userId);
        
        Long remainingSockets = redisTemplate.opsForSet().size(socketsKey);
        log.debug("Removed socket {} for user {}, remaining sockets: {}", sessionId, userId, remainingSockets);
        
        // Return true if no more active sockets
        return remainingSockets == null || remainingSockets == 0;
    }

    public void updateLastActivity(UUID userId) {
        try {
            String key = RedisKeyBuilder.presenceLastActivity(userId);
            redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("Error updating last activity for user: {}", userId, e);
        }
    }

    public void updateHeartbeat(UUID userId) {
        try {
            String key = RedisKeyBuilder.presenceHeartbeat(userId);
            redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("Error updating heartbeat for user: {}", userId, e);
        }
    }

    public boolean shouldAway(UUID userId) {
        try {
            String key = RedisKeyBuilder.presenceLastActivity(userId);
            Object lastActivity = redisTemplate.opsForValue().get(key);

            if (lastActivity == null) {
                return false;
            }

            long idle = System.currentTimeMillis() - Long.parseLong(lastActivity.toString());
            log.debug("User {} is away for {} seconds", userId, idle);
            return idle >= AWAY_THRESHOLD_MS;
        } catch (Exception e) {
            log.error("Error checking away status for user: {}", userId, e);
            return false;
        }
    }

    public boolean shouldOffline(UUID userId) {
        try {
            // Check if there are any active sockets
            long socketsCount = getActiveSocketsCount(userId);
            if (socketsCount > 0) {
                return false;
            }

            // No active sockets, check heartbeat timeout
            String heartbeatKey = RedisKeyBuilder.presenceHeartbeat(userId);
            Object lastHeartbeat = redisTemplate.opsForValue().get(heartbeatKey);

            if (lastHeartbeat == null) {
                return true;
            }

            long idle = System.currentTimeMillis() - Long.parseLong(lastHeartbeat.toString());
            return idle >= HEARTBEAT_TIMEOUT_MS;
        } catch (Exception e) {
            log.error("Error checking offline status for user: {}", userId, e);
            return false;
        }
    }

    public void setStatus(UUID userId, UserStatus status) {
        try {
            String key = RedisKeyBuilder.presenceStatus(userId);
            redisTemplate.opsForValue().set(key, status.name());
            log.debug("Set Redis status for user {}: {}", userId, status);
            
            // Update online users set
            String onlineUsersKey = RedisKeyBuilder.presenceOnlineUsers();
            if (status == UserStatus.ONLINE || status == UserStatus.AWAY) {
                redisTemplate.opsForSet().add(onlineUsersKey, userId.toString());
            } else {
                redisTemplate.opsForSet().remove(onlineUsersKey, userId.toString());
            }
        } catch (Exception e) {
            log.error("Error setting status for user: {}", userId, e);
        }
    }

    public String getStatus(UUID userId) {
        try {
            String statusKey = RedisKeyBuilder.presenceStatus(userId);
            Object status = redisTemplate.opsForValue().get(statusKey);
            
            if (status != null) {
                String statusStr = status.toString();
                log.debug("Got Redis status for user {}: {}", userId, statusStr);
                return statusStr;
            }
            
            // Default to OFFLINE
            return "OFFLINE";
        } catch (Exception e) {
            log.error("Error getting status for user: {}", userId, e);
            return "OFFLINE";
        }
    }

    public long getActiveSocketsCount(UUID userId) {
        try {
            String socketsKey = RedisKeyBuilder.presenceSockets(userId);
            Long count = redisTemplate.opsForSet().size(socketsKey);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Error getting sockets count for user: {}", userId, e);
            return 0;
        }
    }

    public void clearUserPresence(UUID userId) {
        try {
            redisTemplate.delete(RedisKeyBuilder.presenceStatus(userId));
            redisTemplate.delete(RedisKeyBuilder.presenceLastActivity(userId));
            redisTemplate.delete(RedisKeyBuilder.presenceHeartbeat(userId));
            redisTemplate.delete(RedisKeyBuilder.presenceSockets(userId));
            redisTemplate.opsForSet().remove(RedisKeyBuilder.presenceOnlineUsers(), userId.toString());
            log.debug("Cleared all presence data for user: {}", userId);
        } catch (Exception e) {
            log.error("Error clearing presence for user: {}", userId, e);
        }
    }
}
