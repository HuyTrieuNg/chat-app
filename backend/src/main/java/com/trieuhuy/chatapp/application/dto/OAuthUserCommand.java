package com.trieuhuy.chatapp.application.dto;

import com.trieuhuy.chatapp.domain.model.AuthProvider;

public record OAuthUserCommand(
        String email,
        String name,
        AuthProvider provider,
        String providerId
) {}
