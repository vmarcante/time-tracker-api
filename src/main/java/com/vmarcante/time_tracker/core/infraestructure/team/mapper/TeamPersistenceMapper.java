package com.vmarcante.time_tracker.core.infraestructure.team.mapper;

import com.vmarcante.time_tracker.core.domain.team.model.Team;
import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;
import com.vmarcante.time_tracker.core.infraestructure.team.persistence.TeamJpaEntity;
import com.vmarcante.time_tracker.core.infraestructure.team.persistence.UserTeamJpaEntity;

public class TeamPersistenceMapper {

    public static Team toDomain(TeamJpaEntity entity) {
        Team team = new Team();
        team.setId(entity.getId());
        team.setSeqId(entity.getSeqId());
        team.setCompanyId(entity.getCompanyId());
        team.setName(entity.getName());
        team.setDescription(entity.getDescription());
        team.setActive(entity.getActive());
        team.setCreatedAt(entity.getCreatedAt());
        team.setUpdatedAt(entity.getUpdatedAt());
        team.setCreatedBy(entity.getCreatedBy());
        team.setUpdatedBy(entity.getUpdatedBy());
        return team;
    }

    public static TeamJpaEntity toEntity(Team team) {
        TeamJpaEntity entity = new TeamJpaEntity();
        entity.setId(team.getId());
        entity.setCompanyId(team.getCompanyId());
        entity.setName(team.getName());
        entity.setDescription(team.getDescription());
        entity.setActive(team.getActive());
        entity.setCreatedBy(team.getCreatedBy());
        entity.setUpdatedBy(team.getUpdatedBy());
        return entity;
    }

    public static TeamMembership toDomain(UserTeamJpaEntity entity) {
        TeamMembership membership = new TeamMembership();
        membership.setId(entity.getId());
        membership.setSeqId(entity.getSeqId());
        membership.setUserId(entity.getUserId());
        membership.setTeamId(entity.getTeamId());
        membership.setRole(entity.getRole());
        membership.setApproved(entity.getApproved());
        membership.setActive(entity.getActive());
        membership.setCreatedAt(entity.getCreatedAt());
        membership.setUpdatedAt(entity.getUpdatedAt());
        membership.setCreatedBy(entity.getCreatedBy());
        membership.setUpdatedBy(entity.getUpdatedBy());
        return membership;
    }

    public static UserTeamJpaEntity toEntity(TeamMembership membership) {
        UserTeamJpaEntity entity = new UserTeamJpaEntity();
        entity.setId(membership.getId());
        entity.setUserId(membership.getUserId());
        entity.setTeamId(membership.getTeamId());
        entity.setRole(membership.getRole());
        entity.setApproved(membership.getApproved());
        entity.setActive(membership.getActive());
        entity.setCreatedBy(membership.getCreatedBy());
        entity.setUpdatedBy(membership.getUpdatedBy());
        return entity;
    }
}
