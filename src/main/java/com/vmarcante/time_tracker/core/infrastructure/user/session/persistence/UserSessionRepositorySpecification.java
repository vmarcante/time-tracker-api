package com.vmarcante.time_tracker.core.infrastructure.user.session.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.vmarcante.time_tracker.core.domain.user.session.model.filter.UserSessionFilter;
import com.vmarcante.time_tracker.core.shared.utils.TokenHashUtils;

public final class UserSessionRepositorySpecification {

    private UserSessionRepositorySpecification() {
    }

    public static Specification<UserSessionJpaEntity> withFilter(UserSessionFilter filter) {
        if (filter == null) {
            return Specification.unrestricted();
        }
        return Specification.allOf(List.of(
                hasId(filter.getId()),
                hasUserId(filter.getUserId()),
                hasRefreshToken(filter.getRefreshToken()),
                hasRefreshTokenHash(filter.getRefreshTokenHash()),
                isActive(filter.getActive())));
    }

    public static Specification<UserSessionJpaEntity> hasId(UUID id) {
        return (root, query, cb) -> id == null
                ? cb.conjunction()
                : cb.equal(root.get("id"), id);
    }

    public static Specification<UserSessionJpaEntity> hasUserId(UUID userId) {
        return (root, query, cb) -> userId == null
                ? cb.conjunction()
                : cb.equal(root.get("userId"), userId);
    }

    public static Specification<UserSessionJpaEntity> hasRefreshToken(String refreshToken) {
        return (root, query, cb) -> refreshToken == null || refreshToken.isBlank()
                ? cb.conjunction()
                : cb.equal(root.get("refreshTokenHash"), TokenHashUtils.generateHash(refreshToken));
    }

    public static Specification<UserSessionJpaEntity> hasRefreshTokenHash(String refreshTokenHash) {
        return (root, query, cb) -> refreshTokenHash == null || refreshTokenHash.isBlank()
                ? cb.conjunction()
                : cb.equal(root.get("refreshTokenHash"), refreshTokenHash);
    }

    public static Specification<UserSessionJpaEntity> isActive(Boolean active) {
        return (root, query, cb) -> active == null
                ? cb.conjunction()
                : cb.equal(root.get("active"), active);
    }
}
