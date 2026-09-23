package com.vmarcante.time_tracker.core.domain.project.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.model.ProjectAssignment;
import com.vmarcante.time_tracker.core.domain.project.model.TeamProject;

public interface ProjectRepository {

    Project save(Project project);

    Optional<Project> findById(UUID id);

    Page<Project> findActiveByCompanyId(UUID companyId, Pageable pageable);

    Page<Project> findActiveByUserId(UUID userId, Pageable pageable);

    Page<Project> findActiveProjectsByUserId(UUID userId, UUID companyId, Pageable pageable);

    Page<Project> findActiveByTeamId(UUID teamId, Pageable pageable);

    boolean existsActiveByIdAndCompanyId(UUID projectId, UUID companyId);

    boolean existsActiveByCompanyIdAndName(UUID companyId, String name);

    boolean existsActiveByUserIdAndName(UUID userId, String name);

    Map<UUID, String> findNamesByIds(Collection<UUID> projectIds);

    List<Project> findAllByIds(Collection<UUID> projectIds);

    TeamProject saveTeamLink(TeamProject link);

    Optional<TeamProject> findActiveTeamLink(UUID projectId, UUID teamId);

    boolean isProjectLinkedToTeam(UUID projectId, UUID teamId);

    Map<UUID, Long> countActiveTeamsByProjectIds(Collection<UUID> projectIds);

    void deactivateTeamLinksByProjectId(UUID projectId, UUID updatedBy);

    void deactivateTeamLinksByTeamId(UUID teamId, UUID updatedBy);

    ProjectAssignment saveAssignment(ProjectAssignment assignment);

    Optional<ProjectAssignment> findAssignmentById(UUID assignmentId);

    boolean hasActiveAssignment(UUID userId, UUID projectId, UUID teamId);

    boolean hasAnyActiveAssignment(UUID userId, UUID projectId);

    Page<ProjectAssignment> findActiveAssignmentsByProjectId(UUID projectId, Pageable pageable);

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
