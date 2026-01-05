package com.trieuhuy.chatapp.domain.repository;

import com.trieuhuy.chatapp.domain.model.User;
import com.trieuhuy.chatapp.domain.model.UserSearchCriteria;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    User save(User user);
    void delete(User user);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Page<@NonNull User> findAll(UserSearchCriteria criteria, Pageable pageable);
}
