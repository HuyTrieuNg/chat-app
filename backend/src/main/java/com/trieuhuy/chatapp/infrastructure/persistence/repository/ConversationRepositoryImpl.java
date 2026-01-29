package com.trieuhuy.chatapp.infrastructure.persistence.repository;

import com.trieuhuy.chatapp.domain.model.Conversation;
import com.trieuhuy.chatapp.domain.repository.ConversationRepository;
import com.trieuhuy.chatapp.infrastructure.persistence.entity.ConversationEntity;
import com.trieuhuy.chatapp.infrastructure.persistence.entity.ConversationMemberEntity;
import com.trieuhuy.chatapp.infrastructure.persistence.mapper.ConversationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Primary
@RequiredArgsConstructor
public class ConversationRepositoryImpl implements ConversationRepository {

    private final ConversationJpaRepository jpaRepository;
    private final ConversationMemberJpaRepository memberJpaRepository;
    private final ConversationMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Conversation> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(entity -> {
                    List<ConversationMemberEntity> members = 
                            memberJpaRepository.findActiveMembersByConversationId(id);
                    return mapper.toDomain(entity, members);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Conversation> findPrivateConversationBetween(UUID userId1, UUID userId2) {
        return jpaRepository.findPrivateConversationBetween(userId1, userId2)
                .map(entity -> {
                    List<ConversationMemberEntity> members = 
                            memberJpaRepository.findActiveMembersByConversationId(entity.getId());
                    return mapper.toDomain(entity, members);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Conversation> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(entity -> {
                    List<ConversationMemberEntity> members = 
                            memberJpaRepository.findActiveMembersByConversationId(entity.getId());
                    return mapper.toDomain(entity, members);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Conversation save(Conversation conversation) {
        ConversationEntity entity = mapper.toEntity(conversation);
        ConversationEntity savedEntity = jpaRepository.save(entity);
        List<ConversationMemberEntity> members = 
                memberJpaRepository.findActiveMembersByConversationId(savedEntity.getId());
        
        return mapper.toDomain(savedEntity, members);
    }

    @Override
    @Transactional
    public void delete(Conversation conversation) {
        ConversationEntity entity = mapper.toEntity(conversation);
        entity.setDeleted(true);
        jpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}

