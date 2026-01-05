package com.trieuhuy.chatapp.domain.service;

import com.trieuhuy.chatapp.domain.model.AuthProvider;
import com.trieuhuy.chatapp.domain.model.User;
import com.trieuhuy.chatapp.domain.model.UserStatus;
import com.trieuhuy.chatapp.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDomainService {
    
    private final UserRepository userRepository;

    public User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User createUser(String username, String email, String encodedPassword) {
        // Validate business rules
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(encodedPassword)
                .provider(AuthProvider.LOCAL)
                .providerId(null)
                .status(UserStatus.OFFLINE)
                .active(true)
                .build();

        return userRepository.save(user);
    }

    public void changeUserStatus(UUID userId, UserStatus newStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.changeStatus(newStatus);
        
        userRepository.save(user);
    }

    public void activateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.activate();
        
        userRepository.save(user);
    }

    public void deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.deactivate();
        
        userRepository.save(user);
    }
}

