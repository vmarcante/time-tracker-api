package com.vmarcante.time_tracker.core.domain.project.model.filter;

import java.util.UUID;

public final class ProjectAssignmentFilterMapper {

    private ProjectAssignmentFilterMapper() {
    }

    public static ProjectAssignmentFilter activeAssignment(UUID userId, UUID projectId) {
        ProjectAssignmentFilter filter = new ProjectAssignmentFilter();
        filter.setUserId(userId);
        filter.setProjectId(projectId);
        filter.setActive(true);
        return filter;
    }

    public static ProjectAssignmentFilter activeAssignment(UUID userId, UUID projectId, UUID teamId) {
        ProjectAssignmentFilter filter = activeAssignment(userId, projectId);
        filter.setTeamId(teamId);
        return filter;
    }

    public static ProjectAssignmentFilter activeByProjectId(UUID projectId) {
        ProjectAssignmentFilter filter = new ProjectAssignmentFilter();
        filter.setProjectId(projectId);
        filter.setActive(true);
        return filter;
    }

    public static ProjectAssignmentFilter activeByProjectIdAndTeamId(UUID projectId, UUID teamId) {
        ProjectAssignmentFilter filter = new ProjectAssignmentFilter();
        filter.setProjectId(projectId);
        filter.setTeamId(teamId);
        filter.setActive(true);
        return filter;
    }

    public static ProjectAssignmentFilter activeByUserIdAndTeamId(UUID userId, UUID teamId) {
        ProjectAssignmentFilter filter = new ProjectAssignmentFilter();
        filter.setUserId(userId);
        filter.setTeamId(teamId);
        filter.setActive(true);
        return filter;
    }
}
