package com.vmarcante.time_tracker.core.domain.project.event;

import java.util.UUID;

import lombok.Data;

@Data
public class ProjectAssignedEvent {

    private final UUID userId;
    private final UUID projectId;
    private final UUID teamId;
    private final UUID companyId;

    public ProjectAssignedEvent(UUID userId, UUID projectId, UUID teamId, UUID companyId) {
        this.userId = userId;
        this.projectId = projectId;
        this.teamId = teamId;
        this.companyId = companyId;
    }
}
