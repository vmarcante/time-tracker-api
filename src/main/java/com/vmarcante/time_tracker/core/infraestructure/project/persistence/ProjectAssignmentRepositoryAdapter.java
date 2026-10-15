package com.vmarcante.time_tracker.core.infraestructure.project.persistence;

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
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.infraestructure.project.mapper.ProjectPersistenceMapper;

@Component
public class ProjectAssignmentRepositoryAdapter implements ProjectAssignmentRepository {

    private final UserProjectJpaRepository userProjectJpaRepository;

    public ProjectAssignmentRepositoryAdapter(UserProjectJpaRepository userProjectJpaRepository) {
        this.userProjectJpaRepository = userProjectJpaRepository;
    }

    @Override
    public ProjectAssignment saveAssignment(ProjectAssignment assignment) {
        UserProjectJpaEntity entity = ProjectPersistenceMapper.toEntity(assignment);
        return ProjectPersistenceMapper.toDomain(userProjectJpaRepository.save(entity));
    }

    @Override
    public List<ProjectAssignment> saveAllAssignments(List<ProjectAssignment> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return List.of();
        }
        List<UserProjectJpaEntity> entities = assignments.stream()
                .map(ProjectPersistenceMapper::toEntity)
                .toList();
        return userProjectJpaRepository.saveAll(entities).stream()
                .map(ProjectPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ProjectAssignment> findAssignmentById(UUID assignmentId) {
        return userProjectJpaRepository.findById(assignmentId)
                .map(ProjectPersistenceMapper::toDomain);
    }

    @Override
    public boolean hasActiveAssignment(UUID userId, UUID projectId, UUID teamId) {
        return userProjectJpaRepository
                .existsByUserIdAndProjectIdAndTeamIdAndActiveTrue(userId, projectId, teamId);
    }

    @Override
    public boolean hasAnyActiveAssignment(UUID userId, UUID projectId) {
        return userProjectJpaRepository.existsByUserIdAndProjectIdAndActiveTrue(userId, projectId);
    }

    @Override
    public Page<ProjectAssignment> findActiveAssignmentsByProjectId(UUID projectId, Pageable pageable) {
        return userProjectJpaRepository.findByProjectIdAndActiveTrue(projectId, pageable)
                .map(ProjectPersistenceMapper::toDomain);
    }

    @Override
    public List<ProjectAssignment> findAllActiveAssignmentsByProjectId(UUID projectId) {
        return userProjectJpaRepository.findByProjectIdAndActiveTrue(projectId).stream()
                .map(ProjectPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<ProjectAssignment> findActiveAssignmentsByUserIdAndTeamId(UUID userId, UUID teamId) {
        return userProjectJpaRepository.findByUserIdAndTeamIdAndActiveTrue(userId, teamId).stream()
                .map(ProjectPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<ProjectAssignment> findActiveAssignmentsByProjectIdAndTeamId(UUID projectId, UUID teamId) {
        return userProjectJpaRepository.findByProjectIdAndTeamIdAndActiveTrue(projectId, teamId).stream()
                .map(ProjectPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<ProjectAssignment> findActiveAssignmentsByUserIdAndCompanyId(UUID userId, UUID companyId) {
        return userProjectJpaRepository.findActiveByUserIdAndCompanyId(userId, companyId).stream()
                .map(ProjectPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Map<UUID, Long> countActiveAssignmentsByProjectIds(Collection<UUID> projectIds) {
        return userProjectJpaRepository.countActiveByProjectIds(projectIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]));
    }

    @Override
    public void deactivateAssignmentsByIds(Collection<UUID> assignmentIds, UUID updatedBy) {
        if (assignmentIds == null || assignmentIds.isEmpty()) {
            return;
        }
        userProjectJpaRepository.deactivateByIdIn(assignmentIds, updatedBy);
    }

    @Override
    public void deactivateAssignmentsByProjectIdAndTeamId(UUID projectId, UUID teamId, UUID updatedBy) {
        userProjectJpaRepository.deactivateByProjectIdAndTeamId(projectId, teamId, updatedBy);
    }

    @Override
    public void deactivateAssignmentsByUserIdAndTeamId(UUID userId, UUID teamId, UUID updatedBy) {
        userProjectJpaRepository.deactivateByUserIdAndTeamId(userId, teamId, updatedBy);
    }

    @Override
    public void deactivateAssignmentsByUserIdAndCompanyId(UUID userId, UUID companyId, UUID updatedBy) {
        userProjectJpaRepository.deactivateByUserIdAndCompanyId(userId, companyId, updatedBy);
    }

    @Override
    public void deactivateAssignmentsByProjectId(UUID projectId, UUID updatedBy) {
        userProjectJpaRepository.deactivateByProjectId(projectId, updatedBy);
    }

    @Override
    public void deactivateAssignmentsByTeamId(UUID teamId, UUID updatedBy) {
        userProjectJpaRepository.deactivateByTeamId(teamId, updatedBy);
    }
}
