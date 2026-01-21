package com.trieuhuy.chatapp.application.dto;

import com.trieuhuy.chatapp.domain.model.User;
import com.trieuhuy.chatapp.domain.model.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String username,
        String email,
        String avatarUrl,
        UserStatus status,
        Instant createdAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
