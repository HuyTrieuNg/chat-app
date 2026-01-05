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

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void changeStatus(UserStatus newStatus) {
        this.status = newStatus;
    }

    public boolean isOnline() {
        return this.status == UserStatus.ONLINE;
    }
}
