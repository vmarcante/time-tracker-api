package com.vmarcante.time_tracker.core.domain.project.model.filter;

import java.util.UUID;

public final class TeamProjectFilterMapper {

    private TeamProjectFilterMapper() {
    }

    public static TeamProjectFilter activeByProjectId(UUID projectId) {
        TeamProjectFilter filter = new TeamProjectFilter();
        filter.setProjectId(projectId);
        filter.setActive(true);
        return filter;
    }

    public static TeamProjectFilter activeLink(UUID projectId, UUID teamId) {
        TeamProjectFilter filter = new TeamProjectFilter();
        filter.setProjectId(projectId);
        filter.setTeamId(teamId);
        filter.setActive(true);
        return filter;
    }
}
