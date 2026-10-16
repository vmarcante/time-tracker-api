package com.vmarcante.time_tracker.core.domain.project.model.filter;

import java.util.Collection;
import java.util.UUID;

public final class ProjectFilterMapper {

    private ProjectFilterMapper() {
    }

    public static ProjectFilter byIds(Collection<UUID> ids) {
        ProjectFilter filter = new ProjectFilter();
        filter.setIds(ids);
        return filter;
    }

    public static ProjectFilter activeByIdAndCompanyId(UUID id, UUID companyId) {
        ProjectFilter filter = new ProjectFilter();
        filter.setId(id);
        filter.setCompanyId(companyId);
        filter.setActive(true);
        return filter;
    }

    public static ProjectFilter activeByCompanyId(UUID companyId) {
        ProjectFilter filter = new ProjectFilter();
        filter.setCompanyId(companyId);
        filter.setActive(true);
        return filter;
    }

    public static ProjectFilter activePersonalOf(UUID userId) {
        ProjectFilter filter = new ProjectFilter();
        filter.setUserId(userId);
        filter.setActive(true);
        return filter;
    }

    public static ProjectFilter activeAssignedToUserInCompany(UUID companyId, UUID userId) {
        ProjectFilter filter = new ProjectFilter();
        filter.setCompanyId(companyId);
        filter.setAssigneeUserId(userId);
        filter.setActive(true);
        return filter;
    }

    public static ProjectFilter activeByNameAndUserId(String name, UUID userId) {
        ProjectFilter filter = new ProjectFilter();
        filter.setName(name);
        filter.setUserId(userId);
        filter.setActive(true);
        return filter;
    }

    public static ProjectFilter activeByNameAndCompanyId(String name, UUID companyId) {
        ProjectFilter filter = new ProjectFilter();
        filter.setName(name);
        filter.setCompanyId(companyId);
        filter.setActive(true);
        return filter;
    }

    public static ProjectFilter activeLinkedToTeam(UUID teamId) {
        ProjectFilter filter = new ProjectFilter();
        filter.setLinkedTeamId(teamId);
        filter.setActive(true);
        return filter;
    }
}
