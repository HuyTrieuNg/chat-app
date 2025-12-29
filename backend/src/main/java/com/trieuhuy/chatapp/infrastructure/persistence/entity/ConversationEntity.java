package com.trieuhuy.chatapp.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "conversations",
        indexes = {
                @Index(name = "idx_conversation_type", columnList = "type")
        }
)
@Getter
@Setter
public class ConversationEntity extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String type; // PRIVATE | GROUP

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @Column(nullable = false)
    private boolean isDeleted = false;
}

