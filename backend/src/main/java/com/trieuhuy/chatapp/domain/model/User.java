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
public class User {
    private UUID id;
    private String username;
    private String email;
    private String passwordHash;
    private AuthProvider provider;
    private String providerId;
    private String avatarUrl;
    private UserStatus status;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastSeenAt;

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void markOnline() {
        if (!active) {
            throw new IllegalStateException("Invalid operation: cannot mark inactive user as online");
        }
        this.status = UserStatus.ONLINE;
        this.lastSeenAt = Instant.now();
    }

    public void markOffline() {
        this.status = UserStatus.OFFLINE;
    }

    public void markAway() {
        if (status == UserStatus.ONLINE) {
            this.status = UserStatus.AWAY;
        }
    }
    
    public boolean isOnline() {
        return this.status == UserStatus.ONLINE;
    }
}
