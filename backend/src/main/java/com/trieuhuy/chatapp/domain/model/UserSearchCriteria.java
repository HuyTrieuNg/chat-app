package com.trieuhuy.chatapp.domain.model;

import java.time.Instant;

public record UserSearchCriteria(
        UserStatus status,
        String keyword,
        Instant from,
        Instant to
) {
}
