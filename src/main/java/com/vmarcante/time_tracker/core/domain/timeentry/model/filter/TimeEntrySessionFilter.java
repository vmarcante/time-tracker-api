package com.vmarcante.time_tracker.core.domain.timeentry.model.filter;

import java.util.Collection;
import java.util.UUID;

import lombok.Data;

@Data
public class TimeEntrySessionFilter {
    private UUID id;
    private UUID entryId;
    private Collection<UUID> entryIds;
    private UUID userId;
    private Boolean running;
    private Boolean active;
}
