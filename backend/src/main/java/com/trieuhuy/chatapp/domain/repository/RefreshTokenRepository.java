package com.trieuhuy.chatapp.domain.repository;

import com.trieuhuy.chatapp.domain.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenAndNotRevoked(String token);

    RefreshToken save(RefreshToken refreshToken);

    void deleteByUserId(UUID userId);

    void deleteByToken(String token);

    void revokeToken(RefreshToken token);

    boolean existsByToken(String token);
}

