package com.vmarcante.time_tracker.core.domain.project.model.filter;

import java.util.Collection;
import java.util.UUID;

import lombok.Data;

@Data
public class ProjectAssignmentFilter {
    private UUID id;
    private Collection<UUID> ids;
    private UUID userId;
    private UUID projectId;
    private UUID teamId;
    private UUID companyId;
    private Boolean active;
}
