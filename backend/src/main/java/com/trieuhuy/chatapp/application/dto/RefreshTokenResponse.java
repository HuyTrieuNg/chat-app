package com.trieuhuy.chatapp.application.dto;

public record RefreshTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
    public RefreshTokenResponse(String accessToken, long expiresIn) {
        this(accessToken, "Bearer", expiresIn);
    }
}

