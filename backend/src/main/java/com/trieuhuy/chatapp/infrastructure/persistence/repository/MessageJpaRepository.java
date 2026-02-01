package com.trieuhuy.chatapp.infrastructure.persistence.repository;

import com.trieuhuy.chatapp.infrastructure.persistence.entity.MessageEntity;
import lombok.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageJpaRepository extends JpaRepository<@NonNull MessageEntity, @NonNull UUID> {
    
    @Query(value = "SELECT m FROM MessageEntity m " +
           "WHERE m.conversation.id = :conversationId " +
           "AND m.isDeleted = false " +
           "AND (:beforeMessageId IS NULL OR m.createdAt < " +
           "  (SELECT m2.createdAt FROM MessageEntity m2 WHERE m2.id = :beforeMessageId)) " +
           "ORDER BY m.createdAt DESC")
    List<MessageEntity> findByConversationIdWithPagination(
            @Param("conversationId") UUID conversationId,
            @Param("beforeMessageId") UUID beforeMessageId,
            Pageable pageable
    );
    
    @Query("SELECT m FROM MessageEntity m " +
           "WHERE m.conversation.id = :conversationId " +
           "AND m.isDeleted = false " +
           "AND m.createdAt > " +
           "  (SELECT m2.createdAt FROM MessageEntity m2 WHERE m2.id = :afterMessageId)" +
           "ORDER BY m.createdAt ASC")
    List<MessageEntity> findByConversationIdAndAfter(
            @Param("conversationId") UUID conversationId,
            @Param("afterMessageId") UUID afterMessageId
    );
    
    long countByConversationIdAndIsDeletedFalse(UUID conversationId);
}

