package com.trieuhuy.chatapp.infrastructure.persistence.mapper;

import com.trieuhuy.chatapp.domain.model.Conversation;
import com.trieuhuy.chatapp.infrastructure.persistence.entity.ConversationEntity;
import com.trieuhuy.chatapp.infrastructure.persistence.entity.ConversationMemberEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConversationMapper {

    public Conversation toDomain(ConversationEntity entity, List<ConversationMemberEntity> members) {
        if (entity == null) {
            return null;
        }

        List<java.util.UUID> memberIds = members != null
                ? members.stream()
                        .map(m -> m.getUser().getId())
                        .collect(Collectors.toList())
                : List.of();

        return Conversation.builder()
                .id(entity.getId())
                .type(entity.getType())
                .name(entity.getName())
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .isDeleted(entity.isDeleted())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .memberIds(memberIds)
                .build();
    }

    public ConversationEntity toEntity(Conversation domain) {
        if (domain == null) {
            return null;
        }

        ConversationEntity entity = new ConversationEntity();
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        entity.setType(domain.getType());
        entity.setName(domain.getName());
        entity.setDeleted(domain.isDeleted());

        return entity;
    }
}

