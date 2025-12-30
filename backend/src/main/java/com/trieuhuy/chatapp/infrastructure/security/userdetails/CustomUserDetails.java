package com.trieuhuy.chatapp.infrastructure.security.userdetails;

import com.trieuhuy.chatapp.infrastructure.persistence.entity.UserEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@NullMarked
public record CustomUserDetails(UserEntity user) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return this.user.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    public UUID getUserId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }
}

