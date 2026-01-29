package com.trieuhuy.chatapp.application.dto;

import com.trieuhuy.chatapp.domain.model.Conversation;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConversationDto(
        UUID id,
        String type,
        String name,
        UUID createdBy,
        boolean isDeleted,
        Instant createdAt,
        Instant updatedAt,
        List<UUID> memberIds,
        Long unreadCount
) {
    public static ConversationDto from(Conversation conversation) {
        return from(conversation, null);
    }

    public static ConversationDto from(Conversation conversation, Long unreadCount) {
        return new ConversationDto(
                conversation.getId(),
                conversation.getType(),
                conversation.getName(),
                conversation.getCreatedBy(),
                conversation.isDeleted(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                conversation.getMemberIds(),
                unreadCount
        );
    }
}

