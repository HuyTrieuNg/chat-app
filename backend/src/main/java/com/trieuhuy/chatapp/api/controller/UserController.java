package com.trieuhuy.chatapp.api.controller;

import com.trieuhuy.chatapp.application.dto.UserResponse;
import com.trieuhuy.chatapp.application.service.UserApplicationService;
import com.trieuhuy.chatapp.domain.model.UserRole;
import com.trieuhuy.chatapp.domain.model.UserSearchCriteria;
import com.trieuhuy.chatapp.domain.model.UserStatus;
import com.trieuhuy.chatapp.infrastructure.security.util.SecurityUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserApplicationService userService;

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

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<@NonNull UserResponse> getUsers(
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            Pageable pageable) {

        if (to != null && from != null && to.isBefore(from)) {
            throw new IllegalArgumentException("'to' timestamp must be after 'from' timestamp");
        }

        UserSearchCriteria criteria =
                new UserSearchCriteria(status, role, keyword, from, to);

        return userService.
                findAll(criteria, pageable)
                .map(UserResponse::from);
    }
}

