package com.trieuhuy.chatapp.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "conversation_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_conversation_user",
                        columnNames = {"conversation_id", "user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_member_user", columnList = "user_id"),
                @Index(name = "idx_member_conversation", columnList = "conversation_id")
        }
)
@Getter
@Setter
public class ConversationMemberEntity {

    @EmbeddedId
    private ConversationMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("conversationId")
    @JoinColumn(name = "conversation_id")
    private ConversationEntity conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(nullable = false)
    private String role; // ADMIN | MEMBER

    @Column(nullable = false)
    private boolean isMuted = false;

    private UUID lastReadMessageId;

    private Instant joinedAt = Instant.now();

    private Instant leftAt;
}

