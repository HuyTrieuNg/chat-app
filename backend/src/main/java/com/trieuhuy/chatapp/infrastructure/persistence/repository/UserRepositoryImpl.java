package com.trieuhuy.chatapp.infrastructure.persistence.repository;

import com.trieuhuy.chatapp.domain.model.User;
import com.trieuhuy.chatapp.domain.model.UserSearchCriteria;
import com.trieuhuy.chatapp.domain.repository.UserRepository;
import com.trieuhuy.chatapp.infrastructure.persistence.entity.UserEntity;
import com.trieuhuy.chatapp.infrastructure.persistence.mapper.UserMapper;
import com.trieuhuy.chatapp.infrastructure.persistence.specification.UserJpaSpecifications;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@Primary
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaUserRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaUserRepository.findByUsername(username)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public Page<@NonNull User> findAll(UserSearchCriteria criteria, Pageable pageable) {

        Specification<@NonNull UserEntity> specification = Specification
                .where(UserJpaSpecifications.hasStatus(String.valueOf(criteria.status())))
                .and(UserJpaSpecifications.hasRole(String.valueOf(criteria.role())))
                .and(UserJpaSpecifications.keyword(criteria.keyword()))
                .and(UserJpaSpecifications.createdFrom(criteria.from()))
                .and(UserJpaSpecifications.createdTo(criteria.to()));

        return jpaUserRepository.findAll(
                        specification,
                        pageable)
                .map(userMapper::toDomain);
    }

    @Override
    public User save(User user) {
        var entity = userMapper.toEntity(user);
        var savedEntity = jpaUserRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public void delete(User user) {
        var entity = userMapper.toEntity(user);
        jpaUserRepository.delete(entity);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaUserRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }
}

