package com.trieuhuy.chatapp.api.controller;

import com.trieuhuy.chatapp.application.dto.BulkPresenceResponse;
import com.trieuhuy.chatapp.application.dto.UserProfileResponse;
import com.trieuhuy.chatapp.application.dto.UserResponse;
import com.trieuhuy.chatapp.application.service.PresenceApplicationService;
import com.trieuhuy.chatapp.application.service.UserApplicationService;
import com.trieuhuy.chatapp.domain.model.User;
import com.trieuhuy.chatapp.domain.model.UserSearchCriteria;
import com.trieuhuy.chatapp.domain.model.UserStatus;
import com.trieuhuy.chatapp.infrastructure.security.util.SecurityUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserApplicationService userManagementService;
    private final PresenceApplicationService presenceService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull Map<String, Object>> getCurrentUser() {
        var userId = SecurityUtils.getCurrentUserId();
        String username = SecurityUtils.getCurrentUsername();
        String email = SecurityUtils.getCurrentUserEmail();

        log.info("Current user - ID: {}, Username: {}, Email: {}", userId, username, email);

        return ResponseEntity.ok(Map.of(
                "userId", userId != null ? userId : "",
                "username", username != null ? username : "",
                "email", email != null ? email : "",
                "authenticated", SecurityUtils.isAuthenticated()
        ));
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull Map<String, String>> getProfile() {
        return ResponseEntity.ok(Map.of(
                "message", "This is a protected endpoint",
                "username", Objects.requireNonNull(SecurityUtils.getCurrentUsername())
        ));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull UserProfileResponse> getUserProfile(@PathVariable UUID userId) {
        log.debug("Getting user profile: userId={}", userId);
        User user = userManagementService.getUserProfile(userId);
        return ResponseEntity.ok(UserProfileResponse.from(user));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<@NonNull UserResponse> getUsers(
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            Pageable pageable) {

        if (to != null && from != null && to.isBefore(from)) {
            throw new IllegalArgumentException("'to' timestamp must be after 'from' timestamp");
        }

        UserSearchCriteria criteria =
                new UserSearchCriteria(status, keyword, from, to);

        return userManagementService
                .searchUsers(criteria, pageable)
                .map(UserResponse::from);
    }

    @PostMapping("/presence")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BulkPresenceResponse> getBulkPresence(@RequestBody List<UUID> userIds) {
        log.debug("Getting bulk presence for {} users", userIds.size());
        Map<UUID, UserStatus> statuses = presenceService.getBulkPresenceStatus(userIds);
        return ResponseEntity.ok(new BulkPresenceResponse(statuses));
    }
}
