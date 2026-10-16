package com.vmarcante.time_tracker.core.infrastructure.timeentry.persistence;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntrySessionFilter;

public final class TimeEntrySessionRepositorySpecification {

    private TimeEntrySessionRepositorySpecification() {
    }

    public static Specification<TimeEntrySessionJpaEntity> withFilter(TimeEntrySessionFilter filter) {
        if (filter == null) {
            return Specification.unrestricted();
        }
        return Specification.allOf(List.of(
                hasId(filter.getId()),
                hasEntryId(filter.getEntryId()),
                hasEntryIdIn(filter.getEntryIds()),
                hasUserId(filter.getUserId()),
                isRunning(filter.getRunning()),
                isActive(filter.getActive()),
                startsBefore(filter.getStartBefore()),
                endsAfter(filter.getEndAfter())));
    }

    public static Specification<TimeEntrySessionJpaEntity> hasId(UUID id) {
        return (root, query, cb) -> id == null
                ? cb.conjunction()
                : cb.equal(root.get("id"), id);
    }

    public static Specification<TimeEntrySessionJpaEntity> hasEntryId(UUID entryId) {
        return (root, query, cb) -> entryId == null
                ? cb.conjunction()
                : cb.equal(root.get("entryId"), entryId);
    }

    public static Specification<TimeEntrySessionJpaEntity> hasEntryIdIn(Collection<UUID> entryIds) {
        return (root, query, cb) -> entryIds == null || entryIds.isEmpty()
                ? cb.conjunction()
                : root.get("entryId").in(entryIds);
    }

    public static Specification<TimeEntrySessionJpaEntity> hasUserId(UUID userId) {
        return (root, query, cb) -> userId == null
                ? cb.conjunction()
                : cb.equal(root.get("userId"), userId);
    }

    public static Specification<TimeEntrySessionJpaEntity> isRunning(Boolean running) {
        return (root, query, cb) -> running == null
                ? cb.conjunction()
                : running ? cb.isNull(root.get("endTime")) : cb.isNotNull(root.get("endTime"));
    }

    public static Specification<TimeEntrySessionJpaEntity> isActive(Boolean active) {
        return (root, query, cb) -> active == null
                ? cb.conjunction()
                : cb.equal(root.get("active"), active);
    }

    public static Specification<TimeEntrySessionJpaEntity> startsBefore(LocalDateTime instant) {
        return (root, query, cb) -> instant == null
                ? cb.conjunction()
                : cb.lessThan(root.get("startTime"), instant);
    }

    public static Specification<TimeEntrySessionJpaEntity> endsAfter(LocalDateTime instant) {
        return (root, query, cb) -> instant == null
                ? cb.conjunction()
                : cb.or(
                        cb.isNull(root.get("endTime")),
                        cb.greaterThan(root.get("endTime"), instant));
    }
}
