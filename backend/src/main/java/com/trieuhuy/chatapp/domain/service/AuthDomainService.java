package com.trieuhuy.chatapp.domain.service;

import com.trieuhuy.chatapp.domain.model.RefreshToken;
import com.trieuhuy.chatapp.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthDomainService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void createRefreshToken(UUID userId, String token, long expirationMillis) {
        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .token(token)
                .expiresAt(Instant.now().plusMillis(expirationMillis))
                .revoked(false)
                .createdAt(Instant.now())
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndNotRevoked(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        return refreshToken;
    }

    public void revokeUserTokens(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::revokeToken);
    }
}

