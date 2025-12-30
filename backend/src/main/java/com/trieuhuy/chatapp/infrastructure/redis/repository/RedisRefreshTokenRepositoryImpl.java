package com.trieuhuy.chatapp.infrastructure.redis.repository;

import com.trieuhuy.chatapp.domain.model.RefreshToken;
import com.trieuhuy.chatapp.domain.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Repository
@Primary
@RequiredArgsConstructor
@Slf4j
public class RedisRefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String TOKEN_PREFIX = "refresh_token:";
    private static final String USER_TOKENS_PREFIX = "user_tokens:";

    @Override
    public RefreshToken save(RefreshToken token) {
        String key = TOKEN_PREFIX + token.getToken();
        
        long ttlSeconds = Duration.between(Instant.now(), token.getExpiresAt()).getSeconds();

        redisTemplate.opsForValue().set(
            key, 
            token, 
            ttlSeconds,
            TimeUnit.SECONDS
        );

        String userKey = USER_TOKENS_PREFIX + token.getUserId();
        redisTemplate.opsForSet().add(userKey, token.getToken());
        
        log.info("Saved refresh token to Redis: {}", token.getToken());
        return token;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        String key = TOKEN_PREFIX + token;
        RefreshToken refreshToken = (RefreshToken) redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByTokenAndNotRevoked(String token) {
        String key = TOKEN_PREFIX + token;
        RefreshToken refreshToken = (RefreshToken) redisTemplate.opsForValue().get(key);
        
        if (refreshToken != null && !refreshToken.isRevoked()) {
            return Optional.of(refreshToken);
        }
        
        return Optional.empty();
    }

    @Override
    public void revokeToken(RefreshToken token) {
        String key = TOKEN_PREFIX + token.getToken();
        
        redisTemplate.delete(key);
        
        String userKey = USER_TOKENS_PREFIX + token.getUserId();
        redisTemplate.opsForSet().remove(userKey, token.getToken());
        
        log.info("Revoked refresh token in Redis: {}", token.getToken());
    }

    @Override
    public void deleteByToken(String token) {
        String key = TOKEN_PREFIX + token;
        
        RefreshToken refreshToken = (RefreshToken) redisTemplate.opsForValue().get(key);
        if (refreshToken != null) {
            String userKey = USER_TOKENS_PREFIX + refreshToken.getUserId();
            redisTemplate.opsForSet().remove(userKey, token);
        }
        
        redisTemplate.delete(key);
        log.info("Deleted refresh token from Redis: {}", token);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        String userKey = USER_TOKENS_PREFIX + userId;
        
        var tokens = redisTemplate.opsForSet().members(userKey);
        
        if (tokens != null) {
            tokens.forEach(token -> {
                String tokenKey = TOKEN_PREFIX + token;
                redisTemplate.delete(tokenKey);
            });
        }
        
        redisTemplate.delete(userKey);
        log.info("Deleted all refresh tokens for user: {}", userId);
    }

    // @Override
    // public List<RefreshToken> findByUserId(UUID userId) {
    //     String userKey = USER_TOKENS_PREFIX + userId;
    //     var tokens = redisTemplate.opsForSet().members(userKey);
        
    //     if (tokens == null) {
    //         return List.of();
    //     }
        
    //     return tokens.stream()
    //         .map(token -> {
    //             String key = TOKEN_PREFIX + token;
    //             return (RefreshToken) redisTemplate.opsForValue().get(key);
    //         })
    //         .filter(token -> token != null)
    //         .toList();
    // }

    @Override
    public boolean existsByToken(String token) {
        String key = TOKEN_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}