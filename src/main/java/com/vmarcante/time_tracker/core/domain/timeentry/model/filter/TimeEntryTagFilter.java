package com.vmarcante.time_tracker.core.domain.timeentry.model.filter;

import java.util.Collection;
import java.util.UUID;

import lombok.Data;

@Data
public class TimeEntryTagFilter {
    private UUID id;
    private UUID entryId;
    private Collection<UUID> entryIds;
    private UUID tagId;
    private Collection<UUID> tagIds;
    private Boolean active;
}
