package com.trieuhuy.chatapp.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.trieuhuy.chatapp.domain.model.User;
import com.trieuhuy.chatapp.domain.model.UserStatus;

import java.util.UUID;

public record UserResponse(
        @JsonProperty("userId") UUID id,
        String username,
        String email,
        String avatarUrl,
        UserStatus status
) {
    public static UserResponse from(User u) {
        return new UserResponse(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getAvatarUrl(),
                u.getStatus()
        );
    }
}
