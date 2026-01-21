package com.trieuhuy.chatapp.infrastructure.redis.presence;

import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisPresenceStore {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PRESENCE_KEY_PREFIX = "presence:conn:";
    private static final String LAST_ACTIVE_KEY_PREFIX = "presence:last_active:";
    private static final long AWAY_THRESHOLD_MS = 5 * 60 * 1000;

    public boolean connect(UUID userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        Long result = redisTemplate.opsForValue().increment(key);

        redisTemplate.opsForValue().set(
           LAST_ACTIVE_KEY_PREFIX + userId, 
           String.valueOf(System.currentTimeMillis()) 
        );

        return result != null && result == 1;
    }

    public boolean disconnect(UUID userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        Long result = redisTemplate.opsForValue().decrement(key);

        redisTemplate.opsForValue().set(
           LAST_ACTIVE_KEY_PREFIX + userId, 
           String.valueOf(System.currentTimeMillis()) 
        );

        return result != null && result <= 0;
    }

    public void touch(UUID userId) {
        try {
            redisTemplate.opsForValue().set(
               LAST_ACTIVE_KEY_PREFIX + userId, 
               String.valueOf(System.currentTimeMillis()) 
            );
        } catch (Exception e) {
            log.error("RedisPresenceStore touch error", e);
        }
    }

    public boolean shouldAway(UUID userId) {
        try {
            String key = LAST_ACTIVE_KEY_PREFIX + userId;
            Object last = redisTemplate.opsForValue().get(key);

            if (last == null) {
                return false;
            }

            long idle = System.currentTimeMillis() - Long.parseLong(last.toString());
            return idle >= AWAY_THRESHOLD_MS;
        } catch (Exception e) {
            return false;
        }
    }

    public String getStatus(UUID userId) {
        try {
            String connKey = PRESENCE_KEY_PREFIX + userId;
            Object connCount = redisTemplate.opsForValue().get(connKey);
            
            if (connCount == null) {
                return "OFFLINE";
            }
            
            long count = Long.parseLong(connCount.toString());
            if (count <= 0) {
                return "OFFLINE";
            }
            
            return shouldAway(userId) ? "AWAY" : "ONLINE";
        } catch (Exception e) {
            log.error("Error getting status for user: {}", userId, e);
            return "OFFLINE";
        }
    }
}
