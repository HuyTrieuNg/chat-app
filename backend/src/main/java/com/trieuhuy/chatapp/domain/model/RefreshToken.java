package com.trieuhuy.chatapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    private UUID id;
    private UUID userId;
    private String token;
    private Instant expiresAt;
    private boolean revoked;
    private Instant createdAt;

    public boolean isExpired() {
        return this.expiresAt.isBefore(Instant.now());
    }

    public boolean isValid() {
        return !this.revoked && !isExpired();
    }

    public void revoke() {
        this.revoked = true;
    }
}

