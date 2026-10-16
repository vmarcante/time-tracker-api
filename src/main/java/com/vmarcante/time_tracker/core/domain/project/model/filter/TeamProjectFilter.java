package com.vmarcante.time_tracker.core.domain.project.model.filter;

import java.util.UUID;

import lombok.Data;

@Data
public class TeamProjectFilter {
    private UUID id;
    private UUID projectId;
    private UUID teamId;
    private Boolean active;
    private Boolean teamActive;
    private Boolean projectActive;
}
