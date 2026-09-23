package com.vmarcante.time_tracker.core.application.project.dto.output;

import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;
import com.vmarcante.time_tracker.core.domain.project.model.Project;

public record ProjectSummaryOutputDTO(
        UUID id,
        String name,
        ProjectStatus status,
        long teamCount,
        long memberCount) {

    public static ProjectSummaryOutputDTO from(Project project, long teamCount, long memberCount) {
        return new ProjectSummaryOutputDTO(
                project.getId(),
                project.getName(),
                project.getStatus(),
                teamCount,
                memberCount);
    }
}
