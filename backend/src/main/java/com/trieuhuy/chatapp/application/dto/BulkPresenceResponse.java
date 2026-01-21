package com.trieuhuy.chatapp.application.dto;

import java.util.Map;
import java.util.UUID;

import com.trieuhuy.chatapp.domain.model.UserStatus;

public record BulkPresenceResponse(
    Map<UUID, UserStatus> statuses
) {
}
