package com.trieuhuy.chatapp.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_username", columnList = "username"),
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_provider_providerId", columnList = "provider, providerId")
        }
)
@Getter
@Setter
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    private String passwordHash;

    private String provider; // LOCAL | GOOGLE | GITHUB

    private String providerId;

    private String avatarUrl;

    @Column(nullable = false)
    private String status; // ONLINE | OFFLINE | AWAY

    @Column
    private Instant lastSeenAt;

    @Column(nullable = false)
    private boolean isActive = true;
}

