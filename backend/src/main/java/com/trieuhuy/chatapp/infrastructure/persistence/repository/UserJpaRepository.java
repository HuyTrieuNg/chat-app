package com.trieuhuy.chatapp.infrastructure.persistence.repository;

import com.trieuhuy.chatapp.infrastructure.persistence.entity.UserEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository
        extends JpaRepository<@NonNull UserEntity,@NonNull UUID>,
                JpaSpecificationExecutor<@NonNull UserEntity>
{
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByProviderAndProviderId(String provider, String providerId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

