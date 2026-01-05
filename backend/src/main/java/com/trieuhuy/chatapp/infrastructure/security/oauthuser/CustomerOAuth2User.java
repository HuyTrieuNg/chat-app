package com.trieuhuy.chatapp.infrastructure.security.oauthuser;

import com.trieuhuy.chatapp.domain.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@NullMarked
public class CustomerOAuth2User implements OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;

    public UUID getId() {
        return user.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_USER");
    }

    @Override
    public String getName() {
        return user.getEmail();
    }
}
