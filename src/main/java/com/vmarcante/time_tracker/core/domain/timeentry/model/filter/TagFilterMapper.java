package com.vmarcante.time_tracker.core.domain.timeentry.model.filter;

import java.util.Collection;
import java.util.UUID;

public final class TagFilterMapper {

    private TagFilterMapper() {
    }

    public static TagFilter activeById(UUID id) {
        TagFilter filter = new TagFilter();
        filter.setId(id);
        filter.setActive(true);
        return filter;
    }

    public static TagFilter activeByUserId(UUID userId) {
        TagFilter filter = new TagFilter();
        filter.setUserId(userId);
        filter.setActive(true);
        return filter;
    }

    public static TagFilter activeByIdsAndUserId(Collection<UUID> ids, UUID userId) {
        TagFilter filter = new TagFilter();
        filter.setIds(ids);
        filter.setUserId(userId);
        filter.setActive(true);
        return filter;
    }

    public static TagFilter activeByNameAndUserId(String name, UUID userId) {
        TagFilter filter = new TagFilter();
        filter.setName(name);
        filter.setUserId(userId);
        filter.setActive(true);
        return filter;
    }
}
