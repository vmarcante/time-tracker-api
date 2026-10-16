package com.vmarcante.time_tracker.core.domain.timeentry.model.filter;

import java.util.Collection;
import java.util.UUID;

public final class TimeEntryTagFilterMapper {

    private TimeEntryTagFilterMapper() {
    }

    public static TimeEntryTagFilter activeByEntryId(UUID entryId) {
        TimeEntryTagFilter filter = new TimeEntryTagFilter();
        filter.setEntryId(entryId);
        filter.setActive(true);
        return filter;
    }

    public static TimeEntryTagFilter activeByTagId(UUID tagId) {
        TimeEntryTagFilter filter = new TimeEntryTagFilter();
        filter.setTagId(tagId);
        filter.setActive(true);
        return filter;
    }

    public static TimeEntryTagFilter activeByEntryIdAndTagIds(UUID entryId, Collection<UUID> tagIds) {
        TimeEntryTagFilter filter = new TimeEntryTagFilter();
        filter.setEntryId(entryId);
        filter.setTagIds(tagIds);
        filter.setActive(true);
        return filter;
    }
}
