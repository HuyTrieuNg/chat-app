package com.trieuhuy.chatapp.api.controller;

import com.trieuhuy.chatapp.infrastructure.security.util.SecurityUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull Map<String, Object>> getCurrentUser() {
        String username = SecurityUtils.getCurrentUsername();
        var userId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.ok(Map.of(
                "userId", userId != null ? userId : "",
                "username", username != null ? username : "",
                "authenticated", SecurityUtils.isAuthenticated()
        ));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<@NonNull Map<String, String>> getProfile() {
        return ResponseEntity.ok(Map.of(
                "message", "This is a protected endpoint",
                "username", Objects.requireNonNull(SecurityUtils.getCurrentUsername())
        ));
    }
}

