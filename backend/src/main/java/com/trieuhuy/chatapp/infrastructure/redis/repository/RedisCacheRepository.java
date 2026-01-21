package com.trieuhuy.chatapp.infrastructure.redis.repository;

import com.trieuhuy.chatapp.domain.repository.CacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
@Slf4j
public class RedisCacheRepository implements CacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public <T> void set(String key, T value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("Cached data: key={}", key);
        } catch (Exception e) {
            log.error("Failed to cache data: key={}", key, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null && type.isInstance(cached)) {
                log.debug("Cache hit: key={}", key);
                return Optional.of((T) cached);
            }
            log.debug("Cache miss: key={}", key);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get cached data: key={}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public void evict(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Evicted cache: key={}", key);
        } catch (Exception e) {
            log.error("Failed to evict cache: key={}", key, e);
        }
    }

@Override
public void evictPattern(String pattern) {
    try {
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        RedisKeyCommands keyCommands = connection.keyCommands();

        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();

        try (Cursor<byte[]> cursor = keyCommands.scan(options)) {

            List<String> keys = new ArrayList<>();

            while (cursor.hasNext()) {
                byte[] keyBytes = cursor.next();
                keys.add(redisTemplate.getStringSerializer().deserialize(keyBytes));
            }

            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Evicted {} keys with pattern {}", keys.size(), pattern);
            }
        }

    } catch (Exception e) {
        log.error("Failed to evict keys for pattern={}", pattern, e);
    }
}

}