package com.trieuhuy.chatapp.infrastructure.security.oauth;

import java.util.Map;

public record GoogleOAuthUserInfo(Map<String, Object> attributes) implements OAuthUserInfo {

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
}
