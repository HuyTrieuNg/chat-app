package com.trieuhuy.chatapp.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record RefreshTokenResponse(
        String accessToken,
        @JsonIgnore String refreshToken,
        String tokenType,
        long expiresIn
) {
    public RefreshTokenResponse(String accessToken, long expiresIn) {
        this(accessToken, null, "Bearer", expiresIn);
    }

    public RefreshTokenResponse(String accessToken, String refreshToken, long expiresIn) {
        this(accessToken, refreshToken, "Bearer", expiresIn);
    }
}

