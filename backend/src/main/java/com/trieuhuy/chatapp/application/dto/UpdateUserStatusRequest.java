package com.trieuhuy.chatapp.application.dto;

import com.trieuhuy.chatapp.domain.model.UserStatus;

public record UpdateUserStatusRequest(
        UserStatus status
) {
}
