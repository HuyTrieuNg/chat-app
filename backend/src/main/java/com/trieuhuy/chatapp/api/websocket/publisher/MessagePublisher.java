package com.trieuhuy.chatapp.api.websocket.publisher;

import com.trieuhuy.chatapp.application.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessagePublisher {

    private final SimpMessagingTemplate template;

    public void publishMessage(UUID conversationId, MessageDto message) {
        String destination = "/topic/conversations/" + conversationId + "/messages";
        log.debug("Publishing message {} to conversation {} via {}", 
                message.id(), conversationId, destination);
        template.convertAndSend(destination, message);
    }


    public void publishMessageToUser(UUID userId, MessageDto message) {
        String destination = "/user/" + userId + "/queue/messages";
        log.debug("Publishing message {} to user {} via {}", 
                message.id(), userId, destination);
        template.convertAndSendToUser(userId.toString(), "/queue/messages", message);
    }
}

