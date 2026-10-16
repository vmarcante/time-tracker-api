package com.vmarcante.time_tracker.core.infrastructure.email.persistence;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.vmarcante.time_tracker.core.domain.email.model.filter.FailedEmailFilter;

public final class FailedEmailRepositorySpecification {

    private FailedEmailRepositorySpecification() {
    }

    public static Specification<FailedEmailJpaEntity> withFilter(FailedEmailFilter filter) {
        if (filter == null) {
            return Specification.unrestricted();
        }
        return Specification.allOf(List.of(
                hasRecipient(filter.getRecipient()),
                hasSubject(filter.getSubject()),
                hasTemplateName(filter.getTemplateName()),
                hasLocale(filter.getLocale()),
                hasRetryCount(filter.getRetryCount()),
                retryCountBelow(filter.getMaxRetryCount()),
                recipientContains(filter.getRecipientContains()),
                subjectContains(filter.getSubjectContains())));
    }

    public static Specification<FailedEmailJpaEntity> hasRecipient(String recipient) {
        return (root, query, cb) -> recipient == null || recipient.isBlank()
                ? cb.conjunction()
                : cb.equal(cb.lower(root.get("recipient")), recipient.toLowerCase());
    }

    public static Specification<FailedEmailJpaEntity> hasSubject(String subject) {
        return (root, query, cb) -> subject == null || subject.isBlank()
                ? cb.conjunction()
                : cb.equal(root.get("subject"), subject);
    }

    public static Specification<FailedEmailJpaEntity> hasTemplateName(String templateName) {
        return (root, query, cb) -> templateName == null || templateName.isBlank()
                ? cb.conjunction()
                : cb.equal(root.get("templateName"), templateName);
    }

    public static Specification<FailedEmailJpaEntity> hasLocale(String locale) {
        return (root, query, cb) -> locale == null || locale.isBlank()
                ? cb.conjunction()
                : cb.equal(root.get("locale"), locale);
    }

    public static Specification<FailedEmailJpaEntity> hasRetryCount(Integer retryCount) {
        return (root, query, cb) -> retryCount == null
                ? cb.conjunction()
                : cb.equal(root.get("retryCount"), retryCount);
    }

    public static Specification<FailedEmailJpaEntity> retryCountBelow(Integer maxRetryCount) {
        return (root, query, cb) -> maxRetryCount == null
                ? cb.conjunction()
                : cb.lessThan(root.get("retryCount"), maxRetryCount);
    }

    public static Specification<FailedEmailJpaEntity> recipientContains(String recipientContains) {
        return (root, query, cb) -> recipientContains == null || recipientContains.isBlank()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("recipient")),
                        "%" + recipientContains.toLowerCase() + "%");
    }

    public static Specification<FailedEmailJpaEntity> subjectContains(String subjectContains) {
        return (root, query, cb) -> subjectContains == null || subjectContains.isBlank()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("subject")),
                        "%" + subjectContains.toLowerCase() + "%");
    }
}
