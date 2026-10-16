package com.vmarcante.time_tracker.core.infraestructure.user.auth.persistence;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.vmarcante.time_tracker.base.infraestructure.persistence.converter.EncryptedFieldHashUtils;
import com.vmarcante.time_tracker.core.domain.user.auth.model.filter.UserAuthFilter;
import com.vmarcante.time_tracker.core.domain.user.enums.UserRoleType;

public final class UserAuthRepositorySpecification {

    private UserAuthRepositorySpecification() {
    }

    public static Specification<UserAuthJpaEntity> withFilter(UserAuthFilter filter) {
        if (filter == null) {
            return Specification.unrestricted();
        }
        return Specification.allOf(List.of(
                hasUsername(filter.getUsername()),
                hasRole(filter.getRole()),
                hasAnyRole(filter.getRoles()),
                isActive(filter.getActive())));
    }

    public static Specification<UserAuthJpaEntity> hasUsername(String username) {
        return (root, query, cb) -> username == null || username.isBlank()
                ? cb.conjunction()
                : cb.equal(root.get("usernameHash"),
                        EncryptedFieldHashUtils.generateSearchHash(username));
    }

    public static Specification<UserAuthJpaEntity> hasRole(UserRoleType role) {
        return (root, query, cb) -> role == null
                ? cb.conjunction()
                : cb.equal(root.get("role"), role);
    }

    public static Specification<UserAuthJpaEntity> hasAnyRole(Collection<UserRoleType> roles) {
        return (root, query, cb) -> roles == null || roles.isEmpty()
                ? cb.conjunction()
                : root.get("role").in(roles);
    }

    public static Specification<UserAuthJpaEntity> isActive(Boolean active) {
        return (root, query, cb) -> active == null
                ? cb.conjunction()
                : cb.equal(root.get("active"), active);
    }
}
