package com.trieuhuy.chatapp.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "message_attachments",
        indexes = {
                @Index(name = "idx_attachment_message", columnList = "message_id")
        }
)
@Getter
@Setter
public class MessageAttachmentEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private MessageEntity message;

    @Column(nullable = false)
    private String fileUrl;

    private String fileName;
    private String fileType;
    private Long fileSize;

    private Instant createdAt = Instant.now();
}

