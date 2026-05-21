package com.vmarcante.time_tracker.core.infraestructure.user.auth.persistence;

import org.springframework.data.jpa.domain.Specification;

import com.vmarcante.time_tracker.core.domain.user.auth.model.filter.UserAuthFilter;

import jakarta.persistence.criteria.Predicate;

public final class UserAuthRepositorySpecification {

    private UserAuthRepositorySpecification() {
    }

    public static Specification<UserAuthJpaEntity> withFilter(UserAuthFilter filter) {
        return (root, query, criteriaBuilder) -> {
            if (filter == null) {
                return criteriaBuilder.conjunction();
            }

            Predicate predicate = criteriaBuilder.conjunction();

            if (filter.getUsername() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("username"), filter.getUsername()));
            } else if (filter.getUsernameContains() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("username")),
                                "%" + filter.getUsernameContains().toLowerCase() + "%"));
            }

            if (filter.getEmail() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("email")),
                                filter.getEmail().toLowerCase()));
            } else if (filter.getEmailContains() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("email")),
                                "%" + filter.getEmailContains().toLowerCase() + "%"));
            }

            if (filter.getRole() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("role"), filter.getRole()));
            } else if (filter.getRoles() != null && !filter.getRoles().isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        root.get("role").in(filter.getRoles()));
            }

            if (filter.getActive() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("active"), filter.getActive()));
            }

            return predicate;
        };
    }
}
