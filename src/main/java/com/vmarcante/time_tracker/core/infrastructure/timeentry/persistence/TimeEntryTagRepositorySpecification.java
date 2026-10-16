package com.vmarcante.time_tracker.core.infrastructure.timeentry.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntryTagFilter;

public final class TimeEntryTagRepositorySpecification {

    private TimeEntryTagRepositorySpecification() {
    }

    public static Specification<TimeEntryTagJpaEntity> withFilter(TimeEntryTagFilter filter) {
        if (filter == null) {
            return Specification.unrestricted();
        }
        return Specification.allOf(List.of(
                hasId(filter.getId()),
                hasEntryId(filter.getEntryId()),
                hasEntryIdIn(filter.getEntryIds()),
                hasTagId(filter.getTagId()),
                hasTagIdIn(filter.getTagIds()),
                isActive(filter.getActive())));
    }

    public static Specification<TimeEntryTagJpaEntity> hasId(UUID id) {
        return (root, query, cb) -> id == null
                ? cb.conjunction()
                : cb.equal(root.get("id"), id);
    }

    public static Specification<TimeEntryTagJpaEntity> hasEntryId(UUID entryId) {
        return (root, query, cb) -> entryId == null
                ? cb.conjunction()
                : cb.equal(root.get("entryId"), entryId);
    }

    public static Specification<TimeEntryTagJpaEntity> hasEntryIdIn(Collection<UUID> entryIds) {
        return (root, query, cb) -> entryIds == null || entryIds.isEmpty()
                ? cb.conjunction()
                : root.get("entryId").in(entryIds);
    }

    public static Specification<TimeEntryTagJpaEntity> hasTagId(UUID tagId) {
        return (root, query, cb) -> tagId == null
                ? cb.conjunction()
                : cb.equal(root.get("tagId"), tagId);
    }

    public static Specification<TimeEntryTagJpaEntity> hasTagIdIn(Collection<UUID> tagIds) {
        return (root, query, cb) -> tagIds == null || tagIds.isEmpty()
                ? cb.conjunction()
                : root.get("tagId").in(tagIds);
    }

    public static Specification<TimeEntryTagJpaEntity> isActive(Boolean active) {
        return (root, query, cb) -> active == null
                ? cb.conjunction()
                : cb.equal(root.get("active"), active);
    }
}
