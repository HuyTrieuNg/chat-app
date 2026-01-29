package com.trieuhuy.chatapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {
    private UUID id;
    private String type; 
    private String name;
    private UUID createdBy;
    private boolean isDeleted;
    private Instant createdAt;
    private Instant updatedAt;
    private List<UUID> memberIds;
}

