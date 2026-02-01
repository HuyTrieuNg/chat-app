package com.trieuhuy.chatapp.application.dto;

import com.trieuhuy.chatapp.domain.model.Message;

import java.time.Instant;
import java.util.UUID;

public record MessageDto(
        UUID id,
        UUID conversationId,
        UUID senderId,
        String senderUsername,
        String type,
        String content,
        UUID replyToMessageId,
        boolean isDeleted,
        Instant createdAt,
        Instant updatedAt
) {
    public static MessageDto from(Message message, String senderUsername) {
        return new MessageDto(
                message.getId(),
                message.getConversationId(),
                message.getSenderId(),
                senderUsername,
                message.getType(),
                message.getContent(),
                message.getReplyToMessageId(),
                message.isDeleted(),
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }
}

