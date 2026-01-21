package com.trieuhuy.chatapp.infrastructure.persistence.mapper;

import com.trieuhuy.chatapp.domain.model.AuthProvider;
import com.trieuhuy.chatapp.domain.model.User;
import com.trieuhuy.chatapp.domain.model.UserStatus;
import com.trieuhuy.chatapp.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .provider(entity.getProvider() != null ? AuthProvider.valueOf(entity.getProvider()) : AuthProvider.LOCAL)
                .providerId(entity.getProviderId())
                .avatarUrl(entity.getAvatarUrl())
                .status(UserStatus.valueOf(entity.getStatus()))
                .lastSeenAt(entity.getLastSeenAt())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        entity.setUsername(domain.getUsername());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setProvider(domain.getProvider() != null ? domain.getProvider().name() : AuthProvider.LOCAL.name());
        entity.setProviderId(domain.getProviderId());
        entity.setAvatarUrl(domain.getAvatarUrl());
        entity.setStatus(domain.getStatus().name());
        entity.setLastSeenAt(domain.getLastSeenAt());
        entity.setActive(domain.isActive());

        return entity;
    }

    public void updateEntity(UserEntity entity, User domain) {
        if (entity == null || domain == null) {
            return;
        }

        entity.setUsername(domain.getUsername());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setAvatarUrl(domain.getAvatarUrl());
        entity.setStatus(domain.getStatus().name());
        entity.setLastSeenAt(domain.getLastSeenAt());
        entity.setActive(domain.isActive());
        entity.setUpdatedAt(domain.getUpdatedAt());
    }
}

