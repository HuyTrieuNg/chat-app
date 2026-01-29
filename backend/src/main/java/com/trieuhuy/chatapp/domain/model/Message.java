package com.trieuhuy.chatapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
    private UUID id;
    private UUID conversationId;
    private UUID senderId;
    private String type; // TEXT | IMAGE | FILE | SYSTEM
    private String content;
    private UUID replyToMessageId;
    private boolean isDeleted;
    private Instant createdAt;
    private Instant updatedAt;
}

