package com.trieuhuy.chatapp.infrastructure.redis.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.trieuhuy.chatapp.domain.model.RefreshToken;

public interface RedisRefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUserId(UUID userId);
    void deleteByToken(String token);
    void deleteByUserId(UUID userId);
    boolean existsByToken(String token);
}
