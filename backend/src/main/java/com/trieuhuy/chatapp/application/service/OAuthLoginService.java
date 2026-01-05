package com.trieuhuy.chatapp.application.service;

import com.trieuhuy.chatapp.application.dto.OAuthUserCommand;
import com.trieuhuy.chatapp.domain.model.User;
import com.trieuhuy.chatapp.domain.model.UserStatus;
import com.trieuhuy.chatapp.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    private final UserRepository userRepository;

    @Transactional
    public User login(OAuthUserCommand cmd) {
        return userRepository.findByProviderAndProviderId(String.valueOf(cmd.provider()), cmd.providerId())
                .orElseGet(() -> createUser(cmd));
    }

    private User createUser(OAuthUserCommand cmd) {
        String username = generateUniqueUsername(cmd.name(), cmd.email());
        String email = cmd.email();

//        if (email == null || email.isBlank()) {
//            email = String.format("%s.%s@oauth.placeholder",
//                cmd.provider().name().toLowerCase(),
//                cmd.providerId()
//            );
//        }

        User newUser = User.builder()
                .username(username)
                .email(email)
                .passwordHash("")
                .provider(cmd.provider())
                .providerId(cmd.providerId())
                .status(UserStatus.ONLINE)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return userRepository.save(newUser);
    }

    private String generateUniqueUsername(String name, String email) {
        String baseUsername;

        if (name != null && !name.isBlank()) {
            baseUsername = name.toLowerCase().replaceAll("\\s+", "_");
        } else if (email != null && !email.isBlank()) {
            baseUsername = email.split("@")[0];
        } else {
            baseUsername = "user";
        }

        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + "_" + counter++;
        }

        return username;
    }
}
