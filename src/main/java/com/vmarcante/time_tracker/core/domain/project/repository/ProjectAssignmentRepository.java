package com.vmarcante.time_tracker.core.domain.project.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.domain.project.model.ProjectAssignment;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectAssignmentFilter;

public interface ProjectAssignmentRepository {

    ProjectAssignment saveAssignment(ProjectAssignment assignment);

    List<ProjectAssignment> saveAllAssignments(List<ProjectAssignment> assignments);

    Optional<ProjectAssignment> findById(UUID assignmentId);

    Optional<ProjectAssignment> findOne(ProjectAssignmentFilter filter);

    List<ProjectAssignment> search(ProjectAssignmentFilter filter);

    Page<ProjectAssignment> search(ProjectAssignmentFilter filter, Pageable pageable);

    boolean exists(ProjectAssignmentFilter filter);

    Map<UUID, Long> countActiveAssignmentsByProjectIds(Collection<UUID> projectIds);

    void deactivateAssignmentsByIds(Collection<UUID> assignmentIds, UUID updatedBy);

    void deactivateAssignmentsByProjectIdAndTeamId(UUID projectId, UUID teamId, UUID updatedBy);

    void deactivateAssignmentsByUserIdAndTeamId(UUID userId, UUID teamId, UUID updatedBy);

    void deactivateAssignmentsByUserIdAndCompanyId(UUID userId, UUID companyId, UUID updatedBy);

    void deactivateAssignmentsByProjectId(UUID projectId, UUID updatedBy);

    void deactivateAssignmentsByTeamId(UUID teamId, UUID updatedBy);
}
