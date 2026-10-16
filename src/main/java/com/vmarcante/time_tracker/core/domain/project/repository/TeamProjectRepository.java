package com.vmarcante.time_tracker.core.domain.project.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.project.model.TeamProject;
import com.vmarcante.time_tracker.core.domain.project.model.filter.TeamProjectFilter;

public interface TeamProjectRepository {

    TeamProject saveTeamLink(TeamProject link);

    List<TeamProject> saveAllTeamLinks(List<TeamProject> links);

    Optional<TeamProject> findOne(TeamProjectFilter filter);

    List<TeamProject> search(TeamProjectFilter filter);

    boolean exists(TeamProjectFilter filter);

    Map<UUID, Long> countActiveTeamsByProjectIds(Collection<UUID> projectIds);

    void deactivateTeamLinksByIds(Collection<UUID> linkIds, UUID updatedBy);

    void deactivateTeamLinksByProjectId(UUID projectId, UUID updatedBy);

    void deactivateTeamLinksByTeamId(UUID teamId, UUID updatedBy);
}
