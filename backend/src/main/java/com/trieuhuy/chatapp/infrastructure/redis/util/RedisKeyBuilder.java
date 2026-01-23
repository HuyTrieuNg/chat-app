package com.trieuhuy.chatapp.infrastructure.redis.util;

import java.util.UUID;

public class RedisKeyBuilder {

    private static final String PRESENCE_PREFIX = "presence";
    private static final String USER_PREFIX = "user";
    private static final String DEBOUNCE_PREFIX = "debounce";
    private static final String DELIMITER = ":";

    private RedisKeyBuilder() {}

    // ==================== Presence Keys ====================

    /**
     * Key for user's online/offline/away status
     * Format: presence:user:{userId}:status
     */
    public static String presenceStatus(UUID userId) {
        return buildKey(PRESENCE_PREFIX, USER_PREFIX, userId.toString(), "status");
    }

    /**
     * Key for user's last activity timestamp
     * Format: presence:user:{userId}:lastActivity
     */
    public static String presenceLastActivity(UUID userId) {
        return buildKey(PRESENCE_PREFIX, USER_PREFIX, userId.toString(), "lastActivity");
    }

    /**
     * Key for user's heartbeat timestamp
     * Format: presence:user:{userId}:heartbeat
     */
    public static String presenceHeartbeat(UUID userId) {
        return buildKey(PRESENCE_PREFIX, USER_PREFIX, userId.toString(), "heartbeat");
    }

    /**
     * Key for tracking active WebSocket connections (Set)
     * Format: presence:user:{userId}:sockets
     */
    public static String presenceSockets(UUID userId) {
        return buildKey(PRESENCE_PREFIX, USER_PREFIX, userId.toString(), "sockets");
    }

    /**
     * Key for set of all currently online users
     * Format: presence:onlineUsers
     */
    public static String presenceOnlineUsers() {
        return buildKey(PRESENCE_PREFIX, "onlineUsers");
    }

    /**
     * Key for debouncing database updates
     * Format: presence:debounce:user:{userId}
     */
    public static String presenceDebounce(UUID userId) {
        return buildKey(PRESENCE_PREFIX, DEBOUNCE_PREFIX, USER_PREFIX, userId.toString());
    }

    // ==================== Cache Keys ====================

    /**
     * Key for user profile cache
     * Format: user:profile:{userId}
     */
    public static String userProfile(UUID userId) {
        return buildKey(USER_PREFIX, "profile", userId.toString());
    }

    /**
     * Pattern for matching all user profile keys
     * Format: user:profile:*
     */
    public static String userProfilePattern() {
        return buildKey(USER_PREFIX, "profile", "*");
    }

    // ==================== Token Keys ====================

    /**
     * Key for refresh token storage
     * Format: refresh_token:{token}
     */
    public static String refreshToken(String token) {
        return buildKey("refresh_token", token);
    }

    /**
     * Key for set of user's refresh tokens
     * Format: user_tokens:{userId}
     */
    public static String userTokens(UUID userId) {
        return buildKey("user_tokens", userId.toString());
    }

    // ==================== Helper Methods ====================

    /**
     * Build a key from multiple parts separated by delimiter
     */
    private static String buildKey(String... parts) {
        return String.join(DELIMITER, parts);
    }

    /**
     * Extract userId from a presence key
     * Returns null if key format is invalid
     */
    public static UUID extractUserIdFromPresenceKey(String key) {
        try {
            String[] parts = key.split(DELIMITER);
            // Format: presence:user:{userId}:...
            if (parts.length >= 3 && PRESENCE_PREFIX.equals(parts[0]) && USER_PREFIX.equals(parts[1])) {
                return UUID.fromString(parts[2]);
            }
        } catch (Exception e) {}
        return null;
    }
}
