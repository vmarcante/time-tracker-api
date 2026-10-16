package com.vmarcante.time_tracker.core.infrastructure.project.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.project.model.ProjectAssignment;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectAssignmentFilter;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.infrastructure.project.mapper.ProjectPersistenceMapper;

@Component
public class ProjectAssignmentRepositoryAdapter implements ProjectAssignmentRepository {

    private final ProjectAssignmentJpaRepository projectAssignmentJpaRepository;

    public ProjectAssignmentRepositoryAdapter(ProjectAssignmentJpaRepository projectAssignmentJpaRepository) {
        this.projectAssignmentJpaRepository = projectAssignmentJpaRepository;
    }

    @Override
    public ProjectAssignment saveAssignment(ProjectAssignment assignment) {
        ProjectAssignmentJpaEntity entity = ProjectPersistenceMapper.toEntity(assignment);
        return ProjectPersistenceMapper.toDomain(projectAssignmentJpaRepository.save(entity));
    }

    @Override
    public List<ProjectAssignment> saveAllAssignments(List<ProjectAssignment> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return List.of();
        }
        List<ProjectAssignmentJpaEntity> entities = assignments.stream()
                .map(ProjectPersistenceMapper::toEntity)
                .toList();
        return projectAssignmentJpaRepository.saveAll(entities).stream()
                .map(ProjectPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ProjectAssignment> findById(UUID assignmentId) {
        return projectAssignmentJpaRepository.findById(assignmentId)
                .map(ProjectPersistenceMapper::toDomain);
    }

    @Override
    public Optional<ProjectAssignment> findOne(ProjectAssignmentFilter filter) {
        return projectAssignmentJpaRepository
                .findOne(ProjectAssignmentRepositorySpecification.withFilter(filter))
                .map(ProjectPersistenceMapper::toDomain);
    }

    @Override
    public List<ProjectAssignment> search(ProjectAssignmentFilter filter) {
        return projectAssignmentJpaRepository
                .findAll(ProjectAssignmentRepositorySpecification.withFilter(filter))
                .stream()
                .map(ProjectPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Page<ProjectAssignment> search(ProjectAssignmentFilter filter, Pageable pageable) {
        return projectAssignmentJpaRepository
                .findAll(ProjectAssignmentRepositorySpecification.withFilter(filter), pageable)
                .map(ProjectPersistenceMapper::toDomain);
    }

    @Override
    public boolean exists(ProjectAssignmentFilter filter) {
        return projectAssignmentJpaRepository
                .exists(ProjectAssignmentRepositorySpecification.withFilter(filter));
    }

    @Override
    public Map<UUID, Long> countActiveAssignmentsByProjectIds(Collection<UUID> projectIds) {
        return projectAssignmentJpaRepository.countActiveByProjectIds(projectIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]));
    }

    @Override
    public void deactivateAssignmentsByIds(Collection<UUID> assignmentIds, UUID updatedBy) {
        if (assignmentIds == null || assignmentIds.isEmpty()) {
            return;
        }
        projectAssignmentJpaRepository.deactivateByIdIn(assignmentIds, updatedBy);
    }

    @Override
    public void deactivateAssignmentsByProjectIdAndTeamId(UUID projectId, UUID teamId, UUID updatedBy) {
        projectAssignmentJpaRepository.deactivateByProjectIdAndTeamId(projectId, teamId, updatedBy);
    }

    @Override
    public void deactivateAssignmentsByUserIdAndTeamId(UUID userId, UUID teamId, UUID updatedBy) {
        projectAssignmentJpaRepository.deactivateByUserIdAndTeamId(userId, teamId, updatedBy);
    }

    @Override
    public void deactivateAssignmentsByUserIdAndCompanyId(UUID userId, UUID companyId, UUID updatedBy) {
        projectAssignmentJpaRepository.deactivateByUserIdAndCompanyId(userId, companyId, updatedBy);
    }

    @Override
    public void deactivateAssignmentsByProjectId(UUID projectId, UUID updatedBy) {
        projectAssignmentJpaRepository.deactivateByProjectId(projectId, updatedBy);
    }

    @Override
    public void deactivateAssignmentsByTeamId(UUID teamId, UUID updatedBy) {
        projectAssignmentJpaRepository.deactivateByTeamId(teamId, updatedBy);
    }
}
