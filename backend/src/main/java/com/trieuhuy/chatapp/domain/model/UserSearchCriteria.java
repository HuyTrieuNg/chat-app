package com.trieuhuy.chatapp.domain.model;

import java.time.Instant;

public record UserSearchCriteria(
        UserStatus status,
        UserRole role,
        String keyword,
        Instant from,
        Instant to
) {
}
