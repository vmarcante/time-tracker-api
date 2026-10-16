package com.vmarcante.time_tracker.core.domain.timeentry.model.filter;

import java.util.Collection;
import java.util.UUID;

public final class TimeEntrySessionFilterMapper {

    private TimeEntrySessionFilterMapper() {
    }

    public static TimeEntrySessionFilter activeById(UUID id) {
        TimeEntrySessionFilter filter = new TimeEntrySessionFilter();
        filter.setId(id);
        filter.setActive(true);
        return filter;
    }

    public static TimeEntrySessionFilter activeByEntryId(UUID entryId) {
        TimeEntrySessionFilter filter = new TimeEntrySessionFilter();
        filter.setEntryId(entryId);
        filter.setActive(true);
        return filter;
    }

    public static TimeEntrySessionFilter activeByEntryIds(Collection<UUID> entryIds) {
        TimeEntrySessionFilter filter = new TimeEntrySessionFilter();
        filter.setEntryIds(entryIds);
        filter.setActive(true);
        return filter;
    }

    public static TimeEntrySessionFilter runningByUser(UUID userId) {
        TimeEntrySessionFilter filter = new TimeEntrySessionFilter();
        filter.setUserId(userId);
        filter.setRunning(true);
        filter.setActive(true);
        return filter;
    }
}
