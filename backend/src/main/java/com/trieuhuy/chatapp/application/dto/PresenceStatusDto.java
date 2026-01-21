package com.trieuhuy.chatapp.application.dto;

import java.util.UUID;

import com.trieuhuy.chatapp.domain.model.UserStatus;

public record PresenceStatusDto(
    UUID userId, 
    UserStatus newStatus
) {
    public PresenceStatusDto(UUID userId, UserStatus newStatus) {
        this.userId = userId;
        this.newStatus = newStatus;
    }
}
