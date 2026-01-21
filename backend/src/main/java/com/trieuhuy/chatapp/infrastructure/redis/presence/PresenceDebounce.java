package com.trieuhuy.chatapp.infrastructure.redis.presence;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PresenceDebounce {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String UPDATE_DB_KEY_PREFIX = "presence:db:update:";

    public boolean shouldUpdateDb(UUID userId) {
        Boolean first = redisTemplate.opsForValue()
            .setIfAbsent(UPDATE_DB_KEY_PREFIX + userId, "1", Duration.ofSeconds(30));
        
        return Boolean.TRUE.equals(first);
    }
}
    