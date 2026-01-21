package com.trieuhuy.chatapp.application.service;

import com.trieuhuy.chatapp.domain.model.User;
import com.trieuhuy.chatapp.domain.model.UserSearchCriteria;
import com.trieuhuy.chatapp.domain.repository.CacheRepository;
import com.trieuhuy.chatapp.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@NullMarked
public class UserApplicationService {

    private final UserRepository userRepository;
    private final CacheRepository cacheRepository;

    private static final String USER_PROFILE_KEY_PREFIX = "user:profile:";
    private static final Duration PROFILE_CACHE_TTL = Duration.ofMinutes(60);

    @Transactional(readOnly = true)
    public User getUserProfile(UUID userId) {
        log.debug("Getting user profile: userId={}", userId);
        String cacheKey = USER_PROFILE_KEY_PREFIX + userId;

        return cacheRepository.get(cacheKey, User.class)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
                    cacheRepository.set(cacheKey, user, PROFILE_CACHE_TTL);
                    return user;
                });
    }

    @Transactional(readOnly = true)
    public Page<User> searchUsers(UserSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching users with criteria: {}, pageable: {}", criteria, pageable);
        return userRepository.findAll(criteria, pageable);
    }

    public void evictUserProfileCache(UUID userId) {
        log.debug("Evicting user profile cache: userId={}", userId);
        cacheRepository.evict(USER_PROFILE_KEY_PREFIX + userId);
    }
}
