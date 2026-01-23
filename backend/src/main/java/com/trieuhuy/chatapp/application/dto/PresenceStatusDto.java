package com.trieuhuy.chatapp.application.dto;

import java.time.Instant;
import java.util.UUID;

import com.trieuhuy.chatapp.domain.model.UserStatus;

public record PresenceStatusDto(
    UUID userId, 
    UserStatus newStatus,
    Instant lastSeen
) {
    public PresenceStatusDto(UUID userId, UserStatus newStatus) {
        this(userId, newStatus, null);
    }
}
