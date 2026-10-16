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

import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectFilter;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.infrastructure.project.mapper.ProjectPersistenceMapper;

@Component
public class ProjectRepositoryAdapter implements ProjectRepository {

    private final ProjectJpaRepository projectJpaRepository;

    public ProjectRepositoryAdapter(ProjectJpaRepository projectJpaRepository) {
        this.projectJpaRepository = projectJpaRepository;
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
    public Optional<Project> findOne(ProjectFilter filter) {
        return projectJpaRepository
                .findOne(ProjectRepositorySpecification.withFilter(filter))
                .map(ProjectPersistenceMapper::toDomain);
    }

    @Override
    public List<Project> search(ProjectFilter filter) {
        return projectJpaRepository
                .findAll(ProjectRepositorySpecification.withFilter(filter))
                .stream()
                .map(ProjectPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Page<Project> search(ProjectFilter filter, Pageable pageable) {
        return projectJpaRepository
                .findAll(ProjectRepositorySpecification.withFilter(filter), pageable)
                .map(ProjectPersistenceMapper::toDomain);
    }

    @Override
    public boolean exists(ProjectFilter filter) {
        return projectJpaRepository.exists(ProjectRepositorySpecification.withFilter(filter));
    }

    @Override
    public Map<UUID, String> findNamesByIds(Collection<UUID> projectIds) {
        return projectJpaRepository.findIdAndNameByIds(projectIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (String) row[1]));
    }
}
