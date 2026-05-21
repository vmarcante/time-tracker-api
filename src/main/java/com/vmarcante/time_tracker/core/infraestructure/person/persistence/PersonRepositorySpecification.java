package com.vmarcante.time_tracker.core.infraestructure.person.persistence;

import org.springframework.data.jpa.domain.Specification;

import com.vmarcante.time_tracker.core.domain.person.model.filter.PersonFilter;

import jakarta.persistence.criteria.Predicate;

public final class PersonRepositorySpecification {

    private PersonRepositorySpecification() {
    }

    public static Specification<PersonJpaEntity> withFilter(PersonFilter filter) {
        return (root, query, criteriaBuilder) -> {
            if (filter == null) {
                return criteriaBuilder.conjunction();
            }

            Predicate predicate = criteriaBuilder.conjunction();

            if (filter.getId() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("id"), filter.getId()));
            }

            if (filter.getName() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("name"), filter.getName()));
            }

            if (filter.getEmail() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("email")),
                                filter.getEmail().toLowerCase()));
            }

            if (filter.getPhone() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("phone"), filter.getPhone()));
            }

            if (filter.getActive() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("active"), filter.getActive()));
            }

            if (filter.getNameContains() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")),
                                "%" + filter.getNameContains().toLowerCase() + "%"));
            }

            if (filter.getEmailContains() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("email")),
                                "%" + filter.getEmailContains().toLowerCase() + "%"));
            }

            return predicate;
        };
    }
}
