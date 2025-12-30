package com.trieuhuy.chatapp.infrastructure.persistence.specification;

import com.trieuhuy.chatapp.infrastructure.persistence.entity.UserEntity;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class UserJpaSpecifications {

    public static Specification<@NonNull UserEntity> hasStatus(String status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<@NonNull UserEntity> hasRole(String role) {
        return (root, query, criteriaBuilder) ->
                role == null ? null : criteriaBuilder.equal(root.get("role"), role);
    }

    public static Specification<@NonNull UserEntity> keyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            String likePattern = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern)
            );
        };
    }

    public static Specification<@NonNull UserEntity> createdFrom(Instant from) {
        return (root, query, criteriaBuilder) ->
            from == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<@NonNull UserEntity> createdTo(Instant to) {
        return (root, query, criteriaBuilder) ->
            to == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}
