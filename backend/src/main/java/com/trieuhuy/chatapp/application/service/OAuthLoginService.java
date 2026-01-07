package com.trieuhuy.chatapp.application.service;

import com.trieuhuy.chatapp.application.dto.OAuthUserCommand;
import com.trieuhuy.chatapp.domain.model.User;
import com.trieuhuy.chatapp.domain.model.UserStatus;
import com.trieuhuy.chatapp.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

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
            baseUsername = slugify(name);
        } else if (email != null && !email.isBlank()) {
            baseUsername = slugify(email.split("@")[0]);
        } else {
            baseUsername = "user";
        }
        String suffix = UUID.randomUUID().toString().substring(0, 4);

        return baseUsername + "_" + suffix;
    }

    private String slugify(String input) {
        String slug = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        return slug.isBlank() ? "user" : slug;
    }
}
