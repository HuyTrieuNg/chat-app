package com.trieuhuy.chatapp.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SendMessageRequest(
        @NotNull(message = "Conversation ID is required")
        UUID conversationId,
        
        @NotBlank(message = "Message content cannot be blank")
        String content,
        
        String type, // TEXT | IMAGE | FILE | SYSTEM
        
        UUID replyToMessageId
) {
    public String getType() {
        return type != null && !type.isBlank() ? type : "TEXT";
    }
}

