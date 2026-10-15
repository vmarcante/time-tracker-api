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

import com.vmarcante.time_tracker.core.domain.project.model.Project;
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
    public Page<Project> findActiveByCompanyId(UUID companyId, Pageable pageable) {
        return projectJpaRepository.findByCompanyIdAndActiveTrue(companyId, pageable)
                .map(ProjectPersistenceMapper::toDomain);
    }

    @Override
    public Page<Project> findActiveByUserId(UUID userId, Pageable pageable) {
        return projectJpaRepository.findByUserIdAndActiveTrue(userId, pageable)
                .map(ProjectPersistenceMapper::toDomain);
    }

    @Override
    public Page<Project> findActiveProjectsByUserId(UUID userId, UUID companyId, Pageable pageable) {
        return userProjectJpaRepository
                .findActiveProjectsByUserIdAndCompanyId(userId, companyId, pageable)
                .map(ProjectPersistenceMapper::toDomain);
    }

    @Override
    public Page<Project> findActiveByTeamId(UUID teamId, Pageable pageable) {
        return projectTeamJpaRepository.findActiveProjectsByTeamId(teamId, pageable)
                .map(ProjectPersistenceMapper::toDomain);
    }

    @Override
    public List<Project> findAllByIds(Collection<UUID> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }
        return projectJpaRepository.findAllById(projectIds)
                .stream()
                .map(ProjectPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsActiveByIdAndCompanyId(UUID projectId, UUID companyId) {
        return projectJpaRepository.existsByIdAndCompanyIdAndActiveTrue(projectId, companyId);
    }

    @Override
    public boolean existsActiveByUserIdAndName(UUID userId, String name) {
        return projectJpaRepository.existsByUserIdAndNameIgnoreCaseAndActiveTrue(userId, name);
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
}
