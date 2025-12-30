package com.trieuhuy.chatapp.infrastructure.persistence.mapper;

import com.trieuhuy.chatapp.domain.model.RefreshToken;
import com.trieuhuy.chatapp.infrastructure.persistence.entity.RefreshTokenEntity;
import com.trieuhuy.chatapp.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenMapper {

    public RefreshToken toDomain(RefreshTokenEntity entity) {
        if (entity == null) {
            return null;
        }

        return RefreshToken.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .token(entity.getToken())
                .expiresAt(entity.getExpiresAt())
                .revoked(entity.isRevoked())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public RefreshTokenEntity toEntity(RefreshToken domain, UserEntity userEntity) {
        if (domain == null) {
            return null;
        }

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(domain.getId());
        entity.setUser(userEntity);
        entity.setToken(domain.getToken());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setRevoked(domain.isRevoked());
        entity.setCreatedAt(domain.getCreatedAt());

        return entity;
    }
}

