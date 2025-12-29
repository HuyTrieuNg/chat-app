package com.trieuhuy.chatapp.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "messages",
        indexes = {
                @Index(
                        name = "idx_message_conversation_created",
                        columnList = "conversation_id, createdAt"
                ),
                @Index(name = "idx_message_sender", columnList = "sender_id")
        }
)
@Getter
@Setter
public class MessageEntity extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ConversationEntity conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity sender;

    @Column(nullable = false)
    private String type; // TEXT | IMAGE | FILE | SYSTEM

    @Column(columnDefinition = "TEXT")
    private String content;

    private UUID replyToMessageId;

    @Column(nullable = false)
    private boolean isDeleted = false;
}

