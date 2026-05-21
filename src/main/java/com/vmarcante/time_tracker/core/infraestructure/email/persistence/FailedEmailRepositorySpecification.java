package com.vmarcante.time_tracker.core.infraestructure.email.persistence;

import org.springframework.data.jpa.domain.Specification;

import com.vmarcante.time_tracker.core.domain.email.model.filter.FailedEmailFilter;

import jakarta.persistence.criteria.Predicate;

public final class FailedEmailRepositorySpecification {

    private FailedEmailRepositorySpecification() {
    }

    public static Specification<FailedEmailJpaEntity> withFilter(FailedEmailFilter filter) {
        return (root, query, criteriaBuilder) -> {
            if (filter == null) {
                return criteriaBuilder.conjunction();
            }

            Predicate predicate = criteriaBuilder.conjunction();

            if (filter.getRecipient() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("recipient")),
                                filter.getRecipient().toLowerCase()));
            }

            if (filter.getSubject() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("subject"), filter.getSubject()));
            }

            if (filter.getTemplateName() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("templateName"), filter.getTemplateName()));
            }

            if (filter.getLocale() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("locale"), filter.getLocale()));
            }

            if (filter.getRetryCount() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("retryCount"), filter.getRetryCount()));
            }

            if (filter.getMaxRetryCount() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.lessThan(root.get("retryCount"), filter.getMaxRetryCount()));
            }

            if (filter.getRecipientContains() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("recipient")),
                                "%" + filter.getRecipientContains().toLowerCase() + "%"));
            }

            if (filter.getSubjectContains() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("subject")),
                                "%" + filter.getSubjectContains().toLowerCase() + "%"));
            }

            return predicate;
        };
    }
}
