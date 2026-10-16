package com.vmarcante.time_tracker.core.domain.team.model.filter;

import java.util.Collection;
import java.util.UUID;

public final class TeamFilterMapper {

    private TeamFilterMapper() {
    }

    public static TeamFilter activeByIdAndCompanyId(UUID id, UUID companyId) {
        TeamFilter filter = new TeamFilter();
        filter.setId(id);
        filter.setCompanyId(companyId);
        filter.setActive(true);
        return filter;
    }

    public static TeamFilter activeByCompanyId(UUID companyId) {
        TeamFilter filter = new TeamFilter();
        filter.setCompanyId(companyId);
        filter.setActive(true);
        return filter;
    }

    public static TeamFilter activeByIds(Collection<UUID> ids) {
        TeamFilter filter = new TeamFilter();
        filter.setIds(ids);
        filter.setActive(true);
        return filter;
    }

    public static TeamFilter activeByNameAndCompanyId(String name, UUID companyId) {
        TeamFilter filter = new TeamFilter();
        filter.setName(name);
        filter.setCompanyId(companyId);
        filter.setActive(true);
        return filter;
    }

    public static TeamFilter activeByMemberUserIdAndCompanyId(UUID userId, UUID companyId) {
        TeamFilter filter = new TeamFilter();
        filter.setMemberUserId(userId);
        filter.setCompanyId(companyId);
        filter.setActive(true);
        return filter;
    }

    public static TeamFilter activeLinkedToProject(UUID projectId) {
        TeamFilter filter = new TeamFilter();
        filter.setLinkedProjectId(projectId);
        filter.setActive(true);
        return filter;
    }
}
