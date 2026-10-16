package com.vmarcante.time_tracker.core.domain.timeentry.model.filter;

import java.time.LocalDateTime;
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
    /** Sessões com startTime anterior a este instante. */
    private LocalDateTime startBefore;
    /** Sessões com endTime posterior a este instante (ou em curso). */
    private LocalDateTime endAfter;
}
