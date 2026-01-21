package com.trieuhuy.chatapp.api.websocket.config;

import java.util.Collections;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.trieuhuy.chatapp.infrastructure.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authToken = accessor.getFirstNativeHeader("Authorization");
            
            if (authToken == null || !authToken.startsWith("Bearer ")) {
                throw new AuthenticationCredentialsNotFoundException("Missing or invalid Authorization header");
            }
            
            String token = authToken.substring(7);
            
            try {
                if (!jwtTokenProvider.isTokenValid(token)) {
                    throw new AuthenticationCredentialsNotFoundException("Token is expired or invalid");
                }
                
                String userId = jwtTokenProvider.extractUserId(token);
                if (userId == null || userId.isBlank()) {
                    throw new AuthenticationCredentialsNotFoundException("UserId not found in token");
                }
                
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, Collections.emptyList()
                );
                accessor.setUser(authentication);
            } catch (AuthenticationCredentialsNotFoundException e) {
                throw e;
            } catch (Exception e) {
                throw new AuthenticationCredentialsNotFoundException("Token validation failed: " + e.getMessage());
            }
        }
        
        return message;
    }
}
