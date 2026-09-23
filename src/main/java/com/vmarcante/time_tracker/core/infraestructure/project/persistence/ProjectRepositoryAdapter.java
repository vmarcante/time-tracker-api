package com.vmarcante.time_tracker.core.infraestructure.project.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.model.ProjectAssignment;
import com.vmarcante.time_tracker.core.domain.project.model.TeamProject;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.infraestructure.project.mapper.ProjectPersistenceMapper;

@Component
public class ProjectRepositoryAdapter implements ProjectRepository {

    private final ProjectJpaRepository projectJpaRepository;
    private final ProjectTeamJpaRepository projectTeamJpaRepository;
    private final UserProjectJpaRepository userProjectJpaRepository;

    public ProjectRepositoryAdapter(
            ProjectJpaRepository projectJpaRepository,
            ProjectTeamJpaRepository projectTeamJpaRepository,
            UserProjectJpaRepository userProjectJpaRepository) {
        this.projectJpaRepository = projectJpaRepository;
        this.projectTeamJpaRepository = projectTeamJpaRepository;
        this.userProjectJpaRepository = userProjectJpaRepository;
    }

    @Override
    public Project save(Project project) {
        ProjectJpaEntity entity = ProjectPersistenceMapper.toEntity(project);
        return ProjectPersistenceMapper.toDomain(projectJpaRepository.save(entity));
    }

    @Override
    public Optional<Project> findById(UUID id) {
        return projectJpaRepository.findById(id)
                .map(ProjectPersistenceMapper::toDomain);
    }

    @Override
    public List<Project> findActiveByCompanyId(UUID companyId) {
        return projectJpaRepository.findByCompanyIdAndActiveTrue(companyId).stream()
                .map(ProjectPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Project> findActiveByIds(Collection<UUID> projectIds) {
        return projectJpaRepository.findAllById(projectIds).stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .map(ProjectPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Project> findActiveProjectsByUserId(UUID userId, UUID companyId) {
        List<UUID> projectIds = projectJpaRepository
                .findAssignedProjectIdsByUserIdAndCompanyId(userId, companyId);
        return projectJpaRepository.findAllById(projectIds).stream()
                .map(ProjectPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsActiveByIdAndCompanyId(UUID projectId, UUID companyId) {
        return projectJpaRepository.existsByIdAndCompanyIdAndActiveTrue(projectId, companyId);
    }

    @Override
    public boolean existsActiveByCompanyIdAndName(UUID companyId, String name) {
        return projectJpaRepository.existsByCompanyIdAndNameIgnoreCaseAndActiveTrue(companyId, name);
    }

    @Override
    public Map<UUID, String> findNamesByIds(Collection<UUID> projectIds) {
        return projectJpaRepository.findIdAndNameByIds(projectIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (String) row[1]));
    }

    @Override
    public TeamProject saveTeamLink(TeamProject link) {
        ProjectTeamJpaEntity entity = ProjectPersistenceMapper.toEntity(link);
        return ProjectPersistenceMapper.toDomain(projectTeamJpaRepository.save(entity));
    }

    @Override
    public Optional<TeamProject> findActiveTeamLink(UUID projectId, UUID teamId) {
        return projectTeamJpaRepository.findByProjectIdAndTeamIdAndActiveTrue(projectId, teamId)
                .map(ProjectPersistenceMapper::toDomain);
    }

    @Override
    public boolean isProjectLinkedToTeam(UUID projectId, UUID teamId) {
        return projectTeamJpaRepository.existsByProjectIdAndTeamIdAndActiveTrue(projectId, teamId);
    }

    @Override
    public List<UUID> findActiveTeamIdsByProjectId(UUID projectId) {
        return projectTeamJpaRepository.findActiveTeamIdsByProjectId(projectId);
    }

    @Override
    public List<UUID> findActiveProjectIdsByTeamId(UUID teamId) {
        return projectTeamJpaRepository.findActiveProjectIdsByTeamId(teamId);
    }

    @Override
    public Map<UUID, Long> countActiveTeamsByProjectIds(Collection<UUID> projectIds) {
        return projectTeamJpaRepository.countActiveTeamsByProjectIds(projectIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]));
    }

    @Override
    public void deactivateTeamLinksByProjectId(UUID projectId, UUID updatedBy) {
        projectTeamJpaRepository.deactivateByProjectId(projectId, updatedBy);
    }

    @Override
    public void deactivateTeamLinksByTeamId(UUID teamId, UUID updatedBy) {
        projectTeamJpaRepository.deactivateByTeamId(teamId, updatedBy);
    }

    @Override
    public ProjectAssignment saveAssignment(ProjectAssignment assignment) {
        UserProjectJpaEntity entity = ProjectPersistenceMapper.toEntity(assignment);
        return ProjectPersistenceMapper.toDomain(userProjectJpaRepository.save(entity));
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
    public List<ProjectAssignment> findActiveAssignmentsByProjectId(UUID projectId) {
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
