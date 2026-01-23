package com.trieuhuy.chatapp.api.websocket.config;

import java.util.Collections;
import java.util.UUID;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.trieuhuy.chatapp.infrastructure.redis.presence.RedisPresenceStore;
import com.trieuhuy.chatapp.infrastructure.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisPresenceStore redisPresenceStore;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            if (accessor.getUser() != null) {
                log.info("WebSocket DISCONNECT command for user: {}, session: {}", 
                        accessor.getUser().getName(), accessor.getSessionId());
            } else {
                log.warn("WebSocket DISCONNECT command with no user (session: {})", 
                        accessor.getSessionId());
            }
        }
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.debug("WebSocket CONNECT command received");
            String authToken = accessor.getFirstNativeHeader("Authorization");
            
            if (authToken == null || !authToken.startsWith("Bearer ")) {
                log.error("WebSocket authentication failed: Missing or invalid Authorization header");
                throw new AuthenticationCredentialsNotFoundException("Missing or invalid Authorization header");
            }
            
            String token = authToken.substring(7);
            log.debug("Token extracted, validating...");
            
            try {
                if (!jwtTokenProvider.isTokenValid(token)) {
                    log.error("WebSocket authentication failed: Token is expired or invalid");
                    throw new AuthenticationCredentialsNotFoundException("Token is expired or invalid");
                }
                
                String userId = jwtTokenProvider.extractUserId(token);
                if (userId == null || userId.isBlank()) {
                    log.error("WebSocket authentication failed: UserId not found in token");
                    throw new AuthenticationCredentialsNotFoundException("UserId not found in token");
                }
                
                log.debug("WebSocket authentication successful for userId: {}", userId);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, Collections.emptyList()
                );
                accessor.setUser(authentication);
            } catch (AuthenticationCredentialsNotFoundException e) {
                throw e;
            } catch (Exception e) {
                log.error("WebSocket authentication failed: {}", e.getMessage(), e);
                throw new AuthenticationCredentialsNotFoundException("Token validation failed: " + e.getMessage());
            }
        }
        
        if (accessor.getUser() != null) {
            try {
                UUID userId = UUID.fromString(accessor.getUser().getName());
                redisPresenceStore.updateHeartbeat(userId);
                log.trace("Updated heartbeat for user: {}", userId);
            } catch (Exception e) {
                log.debug("Could not update heartbeat: {}", e.getMessage());
            }
        }
        
        return message;
    }
}
