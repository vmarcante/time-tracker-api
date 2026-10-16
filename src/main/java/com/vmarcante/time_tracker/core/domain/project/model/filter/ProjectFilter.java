package com.vmarcante.time_tracker.core.domain.project.model.filter;

import java.util.Collection;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;

import lombok.Data;

@Data
public class ProjectFilter {
    private UUID id;
    private Collection<UUID> ids;
    private UUID companyId;
    private UUID userId;
    private String name;
    private ProjectStatus status;
    private Boolean active;
    private UUID assigneeUserId;
    private UUID linkedTeamId;
}
