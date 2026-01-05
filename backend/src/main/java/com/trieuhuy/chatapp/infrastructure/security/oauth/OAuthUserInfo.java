package com.trieuhuy.chatapp.infrastructure.security.oauth;

import java.util.Map;

public interface OAuthUserInfo {
    String getId();
    String getEmail();
    String getName();
    Map<String, Object> attributes();
}
