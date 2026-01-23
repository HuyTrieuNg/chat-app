package com.trieuhuy.chatapp.infrastructure.redis.presence;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.trieuhuy.chatapp.infrastructure.redis.util.RedisKeyBuilder;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PresenceDebounce {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final Duration DEBOUNCE_DURATION = Duration.ofSeconds(30);

    public boolean shouldUpdateDb(UUID userId) {
        String key = RedisKeyBuilder.presenceDebounce(userId);
        Boolean first = redisTemplate.opsForValue()
            .setIfAbsent(key, "1", DEBOUNCE_DURATION);
        
        return Boolean.TRUE.equals(first);
    }
}
