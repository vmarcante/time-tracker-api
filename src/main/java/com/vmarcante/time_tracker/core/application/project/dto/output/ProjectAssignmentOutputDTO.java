package com.vmarcante.time_tracker.core.application.project.dto.output;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.project.model.ProjectAssignment;

public record ProjectAssignmentOutputDTO(
        UUID assignmentId,
        UUID userId,
        String userName,
        UUID projectId,
        String projectName,
        UUID teamId,
        LocalDateTime createdAt) {

    public static ProjectAssignmentOutputDTO from(
            ProjectAssignment assignment, String userName, String projectName) {
        return new ProjectAssignmentOutputDTO(
                assignment.getId(),
                assignment.getUserId(),
                userName,
                assignment.getProjectId(),
                projectName,
                assignment.getTeamId(),
                assignment.getCreatedAt());
    }
}
