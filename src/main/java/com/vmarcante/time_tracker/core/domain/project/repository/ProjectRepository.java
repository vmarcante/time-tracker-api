package com.vmarcante.time_tracker.core.domain.project.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.model.ProjectAssignment;
import com.vmarcante.time_tracker.core.domain.project.model.TeamProject;

public interface ProjectRepository {

    Project save(Project project);

    Optional<Project> findById(UUID id);

    List<Project> findActiveByCompanyId(UUID companyId);

    List<Project> findActiveByIds(Collection<UUID> projectIds);

    List<Project> findActiveProjectsByUserId(UUID userId, UUID companyId);

    boolean existsActiveByIdAndCompanyId(UUID projectId, UUID companyId);

    boolean existsActiveByCompanyIdAndName(UUID companyId, String name);

    Map<UUID, String> findNamesByIds(Collection<UUID> projectIds);

    TeamProject saveTeamLink(TeamProject link);

    Optional<TeamProject> findActiveTeamLink(UUID projectId, UUID teamId);

    boolean isProjectLinkedToTeam(UUID projectId, UUID teamId);

    List<UUID> findActiveTeamIdsByProjectId(UUID projectId);

    List<UUID> findActiveProjectIdsByTeamId(UUID teamId);

    Map<UUID, Long> countActiveTeamsByProjectIds(Collection<UUID> projectIds);

    void deactivateTeamLinksByProjectId(UUID projectId, UUID updatedBy);

    void deactivateTeamLinksByTeamId(UUID teamId, UUID updatedBy);

    ProjectAssignment saveAssignment(ProjectAssignment assignment);

    Optional<ProjectAssignment> findAssignmentById(UUID assignmentId);

    boolean hasActiveAssignment(UUID userId, UUID projectId, UUID teamId);

    List<ProjectAssignment> findActiveAssignmentsByProjectId(UUID projectId);

    List<ProjectAssignment> findActiveAssignmentsByUserIdAndTeamId(UUID userId, UUID teamId);

    List<ProjectAssignment> findActiveAssignmentsByProjectIdAndTeamId(UUID projectId, UUID teamId);

    List<ProjectAssignment> findActiveAssignmentsByUserIdAndCompanyId(UUID userId, UUID companyId);

    Map<UUID, Long> countActiveAssignmentsByProjectIds(Collection<UUID> projectIds);

    void deactivateAssignmentsByProjectIdAndTeamId(UUID projectId, UUID teamId, UUID updatedBy);

    void deactivateAssignmentsByUserIdAndTeamId(UUID userId, UUID teamId, UUID updatedBy);

    void deactivateAssignmentsByUserIdAndCompanyId(UUID userId, UUID companyId, UUID updatedBy);

    void deactivateAssignmentsByProjectId(UUID projectId, UUID updatedBy);

    void deactivateAssignmentsByTeamId(UUID teamId, UUID updatedBy);
}
