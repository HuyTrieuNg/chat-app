package com.trieuhuy.chatapp.api.websocket.listener;

import java.security.Principal;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.trieuhuy.chatapp.application.service.PresenceApplicationService;
import com.trieuhuy.chatapp.infrastructure.redis.presence.RedisPresenceStore;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Slf4j
public class PresenceEventListener {

    private final PresenceApplicationService presenceService;
    private final RedisPresenceStore redisPresenceStore;
    
    // Debounce disconnect events to handle page refresh reconnections
    private final ScheduledExecutorService disconnectScheduler = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<UUID, ScheduledFuture<?>> pendingDisconnects = new ConcurrentHashMap<>();
    private static final long DISCONNECT_DELAY_MS = 5000; // 5 seconds delay
    
    @EventListener
    public void onConnect(SessionConnectedEvent event) {
        log.debug("SessionConnectedEvent received");
        Principal principal = event.getUser();
        String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
        
        if (principal == null || sessionId == null) {
            log.warn("onConnect: Cannot get event user (Principal={}, SessionId={})", principal, sessionId);
            return;
        }
        
        try {
            UUID userId = UUID.fromString(principal.getName());
            log.info("User connected via WebSocket: userId={}, sessionId={}", userId, sessionId);
            
            cancelPendingDisconnect(userId);
            handleUserConnection(userId, sessionId);
            
        } catch (Exception e) {
            log.error("Error handling connect event for principal: {}", principal.getName(), e);
        }
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        log.debug("SessionDisconnectEvent received");
        Principal principal = event.getUser();
        String sessionId = event.getSessionId();
        
        if (principal == null) {
            log.warn("onDisconnect: Principal is null, sessionId={}, closeStatus={}", 
                    sessionId, event.getCloseStatus());
            return;
        }
        
        if (sessionId == null) {
            log.warn("onDisconnect: SessionId is null for principal={}", principal.getName());
            return;
        }
        
        try {
            UUID userId = UUID.fromString(principal.getName());
            log.info("User disconnected from WebSocket: userId={}, sessionId={}, closeStatus={}", 
                    userId, sessionId, event.getCloseStatus());
            
            handleUserDisconnection(userId, sessionId);
            
        } catch (Exception e) {
            log.error("Error handling disconnect event for principal: {}", principal.getName(), e);
        }
    }

    // Cancel any pending disconnect task for a user
    private void cancelPendingDisconnect(UUID userId) {
        ScheduledFuture<?> pendingTask = pendingDisconnects.remove(userId);
        if (pendingTask != null && !pendingTask.isDone()) {
            pendingTask.cancel(false);
            log.debug("Cancelled pending disconnect for user {} (reconnection detected)", userId);
        }
    }

    private void handleUserConnection(UUID userId, String sessionId) {
        boolean isFirstConnection = redisPresenceStore.addSocket(userId, sessionId);
        
        if (isFirstConnection) {
            log.info("First connection for user {}, broadcasting ONLINE status", userId);
            presenceService.userConnected(userId);
        } else {
            log.debug("Additional connection for user {}, total sockets: {}", 
                     userId, redisPresenceStore.getActiveSocketsCount(userId));
        }
    }

    private void handleUserDisconnection(UUID userId, String sessionId) {
        boolean isLastConnection = redisPresenceStore.removeSocket(userId, sessionId);
        
        if (isLastConnection) {
            scheduleDelayedOfflineBroadcast(userId);
        } else {
            log.debug("Partial disconnect for user {}, remaining sockets: {}", 
                     userId, redisPresenceStore.getActiveSocketsCount(userId));
        }
    }

    /**
     * Schedule delayed OFFLINE broadcast to handle page refresh reconnections.
     * The broadcast is delayed by DISCONNECT_DELAY_MS to allow reconnection.
     */
    private void scheduleDelayedOfflineBroadcast(UUID userId) {
        log.debug("Last connection closed for user {}, scheduling delayed OFFLINE broadcast", userId);
        
        ScheduledFuture<?> task = disconnectScheduler.schedule(() -> {
            executeDelayedOfflineBroadcast(userId);
        }, DISCONNECT_DELAY_MS, TimeUnit.MILLISECONDS);
        
        pendingDisconnects.put(userId, task);
    }

    private void executeDelayedOfflineBroadcast(UUID userId) {
        try {
            long socketsCount = redisPresenceStore.getActiveSocketsCount(userId);
            
            if (socketsCount == 0) {
                log.info("Broadcasting OFFLINE status for user {} after delay", userId);
                presenceService.userDisconnected(userId);
            } else {
                log.debug("User {} reconnected during delay, skipping OFFLINE broadcast", userId);
            }
        } catch (Exception e) {
            log.error("Error in delayed disconnect for user: {}", userId, e);
        } finally {
            pendingDisconnects.remove(userId);
        }
    }
}
