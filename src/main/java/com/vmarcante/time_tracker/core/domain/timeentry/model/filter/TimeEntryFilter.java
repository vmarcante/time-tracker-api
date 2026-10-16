package com.vmarcante.time_tracker.core.domain.timeentry.model.filter;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

import lombok.Data;

@Data
public class TimeEntryFilter {
    private UUID userId;
    private UUID companyId;
    private UUID leadScopeUserId;
    private Collection<UUID> userIds;
    private Collection<UUID> teamIds;
    private UUID projectId;
    private Collection<UUID> tagIds;
    private String name;
    private LocalDateTime from;
    private LocalDateTime to;
}
