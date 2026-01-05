package com.trieuhuy.chatapp.infrastructure.security.oauth;

import java.util.Map;

public final class OAuthUserInfoFactory {

    public static OAuthUserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuthUserInfo(attributes);
            case "github" -> new GithubOAuthUserInfo(attributes);
            default -> throw new IllegalArgumentException("Unsupported OAuth provider: " + registrationId);
        };
    }
}
