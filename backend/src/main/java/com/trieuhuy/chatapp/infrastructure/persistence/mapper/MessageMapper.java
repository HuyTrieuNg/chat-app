package com.trieuhuy.chatapp.infrastructure.persistence.mapper;

import com.trieuhuy.chatapp.domain.model.Message;
import com.trieuhuy.chatapp.infrastructure.persistence.entity.MessageEntity;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public Message toDomain(MessageEntity entity) {
        if (entity == null) {
            return null;
        }

        return Message.builder()
                .id(entity.getId())
                .conversationId(entity.getConversation() != null ? entity.getConversation().getId() : null)
                .senderId(entity.getSender() != null ? entity.getSender().getId() : null)
                .type(entity.getType())
                .content(entity.getContent())
                .replyToMessageId(entity.getReplyToMessageId())
                .isDeleted(entity.isDeleted())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public MessageEntity toEntity(Message domain) {
        if (domain == null) {
            return null;
        }

        MessageEntity entity = new MessageEntity();
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        entity.setType(domain.getType());
        entity.setContent(domain.getContent());
        entity.setReplyToMessageId(domain.getReplyToMessageId());
        entity.setDeleted(domain.isDeleted());

        return entity;
    }
}

