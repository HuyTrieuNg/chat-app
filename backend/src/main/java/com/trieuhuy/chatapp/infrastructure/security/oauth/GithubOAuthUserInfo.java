package com.trieuhuy.chatapp.infrastructure.security.oauth;

import java.util.Map;

public record GithubOAuthUserInfo(Map<String, Object> attributes) implements OAuthUserInfo {

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        String name = (String) attributes.get("name");
        if (name == null || name.isBlank()) {
            name = (String) attributes.get("login");
        }
        return name;
    }
}
