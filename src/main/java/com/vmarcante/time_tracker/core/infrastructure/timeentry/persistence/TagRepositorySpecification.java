package com.vmarcante.time_tracker.core.infrastructure.timeentry.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TagFilter;

public final class TagRepositorySpecification {

    private TagRepositorySpecification() {
    }

    public static Specification<TagJpaEntity> withFilter(TagFilter filter) {
        if (filter == null) {
            return Specification.unrestricted();
        }
        return Specification.allOf(List.of(
                hasId(filter.getId()),
                hasIdIn(filter.getIds()),
                hasUserId(filter.getUserId()),
                hasNameIgnoreCase(filter.getName()),
                isActive(filter.getActive())));
    }

    public static Specification<TagJpaEntity> hasId(UUID id) {
        return (root, query, cb) -> id == null
                ? cb.conjunction()
                : cb.equal(root.get("id"), id);
    }

    public static Specification<TagJpaEntity> hasIdIn(Collection<UUID> ids) {
        return (root, query, cb) -> ids == null || ids.isEmpty()
                ? cb.conjunction()
                : root.get("id").in(ids);
    }

    public static Specification<TagJpaEntity> hasUserId(UUID userId) {
        return (root, query, cb) -> userId == null
                ? cb.conjunction()
                : cb.equal(root.get("userId"), userId);
    }

    public static Specification<TagJpaEntity> hasNameIgnoreCase(String name) {
        return (root, query, cb) -> name == null || name.isBlank()
                ? cb.conjunction()
                : cb.equal(cb.lower(root.get("name")), name.toLowerCase());
    }

    public static Specification<TagJpaEntity> isActive(Boolean active) {
        return (root, query, cb) -> active == null
                ? cb.conjunction()
                : cb.equal(root.get("active"), active);
    }
}
