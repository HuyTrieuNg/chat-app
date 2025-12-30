package com.trieuhuy.chatapp.application.dto;

import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UUID userId,
        String username,
        String email
) {
    public LoginResponse(String accessToken, String refreshToken, long expiresIn, UUID userId, String username, String email) {
        this(accessToken, refreshToken, "Bearer", expiresIn, userId, username, email);
    }
}
