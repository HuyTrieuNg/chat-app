package com.trieuhuy.chatapp.infrastructure.persistence.repository;

import com.trieuhuy.chatapp.infrastructure.persistence.entity.ConversationEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationJpaRepository extends JpaRepository<@NonNull ConversationEntity, @NonNull UUID> {
    
    @Query("SELECT c FROM ConversationEntity c " +
           "WHERE c.type = 'PRIVATE' " +
           "AND c.id IN (" +
           "  SELECT cm1.id.conversationId FROM ConversationMemberEntity cm1 " +
           "  WHERE cm1.id.userId = :userId1 " +
           "  AND cm1.id.conversationId IN (" +
           "    SELECT cm2.id.conversationId FROM ConversationMemberEntity cm2 " +
           "    WHERE cm2.id.userId = :userId2" +
           "  )" +
           ") " +
           "AND c.isDeleted = false")
    Optional<ConversationEntity> findPrivateConversationBetween(
            @Param("userId1") UUID userId1,
            @Param("userId2") UUID userId2
    );
    
    @Query("SELECT DISTINCT c FROM ConversationEntity c " +
           "JOIN ConversationMemberEntity cm ON cm.id.conversationId = c.id " +
           "WHERE cm.id.userId = :userId " +
           "AND c.isDeleted = false " +
           "AND cm.leftAt IS NULL")
    List<ConversationEntity> findByUserId(@Param("userId") UUID userId);
}

