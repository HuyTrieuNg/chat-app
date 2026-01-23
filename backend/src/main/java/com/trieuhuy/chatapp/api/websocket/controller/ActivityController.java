package com.trieuhuy.chatapp.api.websocket.controller;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.trieuhuy.chatapp.application.service.PresenceApplicationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


//  WebSocket controller for handling user activity events.
//  Activity events are used to track user action and update lastActivity timestamp.
@Controller
@RequiredArgsConstructor
@Slf4j
public class ActivityController {

    private final PresenceApplicationService presenceService;

    @MessageMapping("/activity")
    public void handleActivity(StompHeaderAccessor headerAccessor) {
        try {
            if (headerAccessor.getUser() == null) {
                log.warn("Received activity from unauthenticated user");
                return;
            }

            UUID userId = UUID.fromString(headerAccessor.getUser().getName());
            log.trace("Received activity from user: {}", userId);
            
            presenceService.updateUserActivity(userId);
        } catch (Exception e) {
            log.error("Error handling activity event", e);
        }
    }
}
