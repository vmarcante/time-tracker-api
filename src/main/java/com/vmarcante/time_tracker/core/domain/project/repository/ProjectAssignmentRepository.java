package com.vmarcante.time_tracker.core.domain.project.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.domain.project.model.ProjectAssignment;

public interface ProjectAssignmentRepository {

    ProjectAssignment saveAssignment(ProjectAssignment assignment);

    List<ProjectAssignment> saveAllAssignments(List<ProjectAssignment> assignments);

    Optional<ProjectAssignment> findAssignmentById(UUID assignmentId);

    boolean hasActiveAssignment(UUID userId, UUID projectId, UUID teamId);

    boolean hasAnyActiveAssignment(UUID userId, UUID projectId);

    Page<ProjectAssignment> findActiveAssignmentsByProjectId(UUID projectId, Pageable pageable);

    List<ProjectAssignment> findAllActiveAssignmentsByProjectId(UUID projectId);

    List<ProjectAssignment> findActiveAssignmentsByUserIdAndTeamId(UUID userId, UUID teamId);

    List<ProjectAssignment> findActiveAssignmentsByProjectIdAndTeamId(UUID projectId, UUID teamId);

    List<ProjectAssignment> findActiveAssignmentsByUserIdAndCompanyId(UUID userId, UUID companyId);

    Map<UUID, Long> countActiveAssignmentsByProjectIds(Collection<UUID> projectIds);

    void deactivateAssignmentsByIds(Collection<UUID> assignmentIds, UUID updatedBy);

    void deactivateAssignmentsByProjectIdAndTeamId(UUID projectId, UUID teamId, UUID updatedBy);

    void deactivateAssignmentsByUserIdAndTeamId(UUID userId, UUID teamId, UUID updatedBy);

    void deactivateAssignmentsByUserIdAndCompanyId(UUID userId, UUID companyId, UUID updatedBy);

    void deactivateAssignmentsByProjectId(UUID projectId, UUID updatedBy);

    void deactivateAssignmentsByTeamId(UUID teamId, UUID updatedBy);
}
