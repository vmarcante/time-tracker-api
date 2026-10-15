package com.vmarcante.time_tracker.core.domain.project.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.project.model.TeamProject;

public interface TeamProjectRepository {

    TeamProject saveTeamLink(TeamProject link);

    List<TeamProject> saveAllTeamLinks(List<TeamProject> links);

    Optional<TeamProject> findActiveTeamLink(UUID projectId, UUID teamId);

    List<TeamProject> findActiveTeamLinksByProjectId(UUID projectId);

    boolean isProjectLinkedToTeam(UUID projectId, UUID teamId);

    Map<UUID, Long> countActiveTeamsByProjectIds(Collection<UUID> projectIds);

    void deactivateTeamLinksByIds(Collection<UUID> linkIds, UUID updatedBy);

    void deactivateTeamLinksByProjectId(UUID projectId, UUID updatedBy);

    void deactivateTeamLinksByTeamId(UUID teamId, UUID updatedBy);
}
