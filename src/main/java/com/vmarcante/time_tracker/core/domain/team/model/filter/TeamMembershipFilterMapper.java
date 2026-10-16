package com.vmarcante.time_tracker.core.domain.team.model.filter;

import java.util.Collection;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.team.enums.TeamRole;

public final class TeamMembershipFilterMapper {

    private TeamMembershipFilterMapper() {
    }


    public static TeamMembershipFilter activeMembership(UUID userId, UUID teamId) {
        TeamMembershipFilter filter = new TeamMembershipFilter();
        filter.setUserId(userId);
        filter.setTeamId(teamId);
        filter.setActive(true);
        return filter;
    }

    public static TeamMembershipFilter approvedMembership(UUID userId, UUID teamId) {
        TeamMembershipFilter filter = activeMembership(userId, teamId);
        filter.setApproved(true);
        return filter;
    }

    public static TeamMembershipFilter approvedMembershipWithRole(UUID userId, UUID teamId, TeamRole role) {
        TeamMembershipFilter filter = approvedMembership(userId, teamId);
        filter.setRole(role);
        return filter;
    }

    public static TeamMembershipFilter leadByTeam(UUID teamId) {
        TeamMembershipFilter filter = new TeamMembershipFilter();
        filter.setTeamId(teamId);
        filter.setRole(TeamRole.LEAD);
        filter.setApproved(true);
        filter.setActive(true);
        return filter;
    }

    public static TeamMembershipFilter approvedByTeam(UUID teamId) {
        TeamMembershipFilter filter = new TeamMembershipFilter();
        filter.setTeamId(teamId);
        filter.setApproved(true);
        filter.setActive(true);
        return filter;
    }

    public static TeamMembershipFilter approvedByTeamIds(Collection<UUID> teamIds) {
        TeamMembershipFilter filter = new TeamMembershipFilter();
        filter.setTeamIds(teamIds);
        filter.setApproved(true);
        filter.setActive(true);
        return filter;
    }

    public static TeamMembershipFilter activeByUserAndCompany(UUID userId, UUID companyId) {
        TeamMembershipFilter filter = new TeamMembershipFilter();
        filter.setUserId(userId);
        filter.setCompanyId(companyId);
        filter.setActive(true);
        return filter;
    }
}
