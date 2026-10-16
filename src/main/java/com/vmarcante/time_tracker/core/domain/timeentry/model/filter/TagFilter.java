package com.vmarcante.time_tracker.core.domain.timeentry.model.filter;

import java.util.Collection;
import java.util.UUID;

import lombok.Data;

@Data
public class TagFilter {
    private UUID id;
    private Collection<UUID> ids;
    private UUID userId;
    private String name;
    private Boolean active;
}
