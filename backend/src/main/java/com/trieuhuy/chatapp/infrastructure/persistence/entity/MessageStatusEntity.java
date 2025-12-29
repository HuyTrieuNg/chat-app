package com.trieuhuy.chatapp.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "message_status",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_message_user",
                        columnNames = {"message_id", "user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_status_user", columnList = "user_id")
        }
)
@Getter
@Setter
public class MessageStatusEntity extends BaseEntity {

    @EmbeddedId
    private MessageStatusId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("messageId")
    @JoinColumn(name = "message_id")
    private MessageEntity message;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(nullable = false)
    private String status; // SENT | DELIVERED | READ
}

