package com.trieuhuy.chatapp.infrastructure.persistence.repository;

import com.trieuhuy.chatapp.infrastructure.persistence.entity.ConversationMemberEntity;
import com.trieuhuy.chatapp.infrastructure.persistence.entity.ConversationMemberId;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationMemberJpaRepository extends JpaRepository<@NonNull ConversationMemberEntity, @NonNull ConversationMemberId> {
    
    List<ConversationMemberEntity> findById_ConversationId(UUID conversationId);
    
    @Query("SELECT cm FROM ConversationMemberEntity cm " +
           "WHERE cm.id.conversationId = :conversationId " +
           "AND cm.leftAt IS NULL")
    List<ConversationMemberEntity> findActiveMembersByConversationId(@Param("conversationId") UUID conversationId);
}

