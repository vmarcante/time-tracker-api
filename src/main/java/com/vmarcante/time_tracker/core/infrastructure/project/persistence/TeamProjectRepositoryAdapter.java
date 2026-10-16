package com.vmarcante.time_tracker.core.infrastructure.project.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.project.model.TeamProject;
import com.vmarcante.time_tracker.core.domain.project.repository.TeamProjectRepository;
import com.vmarcante.time_tracker.core.infrastructure.project.mapper.ProjectPersistenceMapper;

@Component
public class TeamProjectRepositoryAdapter implements TeamProjectRepository {

    private final ProjectTeamJpaRepository projectTeamJpaRepository;

    public TeamProjectRepositoryAdapter(ProjectTeamJpaRepository projectTeamJpaRepository) {
        this.projectTeamJpaRepository = projectTeamJpaRepository;
    }

    @Override
    public TeamProject saveTeamLink(TeamProject link) {
        ProjectTeamJpaEntity entity = ProjectPersistenceMapper.toEntity(link);
        return ProjectPersistenceMapper.toDomain(projectTeamJpaRepository.save(entity));
    }

    @Override
    public List<TeamProject> saveAllTeamLinks(List<TeamProject> links) {
        if (links == null || links.isEmpty()) {
            return List.of();
        }
        List<ProjectTeamJpaEntity> entities = links.stream()
                .map(ProjectPersistenceMapper::toEntity)
                .toList();
        return projectTeamJpaRepository.saveAll(entities).stream()
                .map(ProjectPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<TeamProject> findActiveTeamLink(UUID projectId, UUID teamId) {
        return projectTeamJpaRepository.findByProjectIdAndTeamIdAndActiveTrue(projectId, teamId)
                .map(ProjectPersistenceMapper::toDomain);
    }

    @Override
    public List<TeamProject> findActiveTeamLinksByProjectId(UUID projectId) {
        return projectTeamJpaRepository.findByProjectIdAndActiveTrue(projectId).stream()
                .map(ProjectPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean isProjectLinkedToTeam(UUID projectId, UUID teamId) {
        return projectTeamJpaRepository.existsByProjectIdAndTeamIdAndActiveTrue(projectId, teamId);
    }

    @Override
    public Map<UUID, Long> countActiveTeamsByProjectIds(Collection<UUID> projectIds) {
        return projectTeamJpaRepository.countActiveTeamsByProjectIds(projectIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]));
    }

    @Override
    public void deactivateTeamLinksByIds(Collection<UUID> linkIds, UUID updatedBy) {
        if (linkIds == null || linkIds.isEmpty()) {
            return;
        }
        projectTeamJpaRepository.deactivateByIdIn(linkIds, updatedBy);
    }

    @Override
    public void deactivateTeamLinksByProjectId(UUID projectId, UUID updatedBy) {
        projectTeamJpaRepository.deactivateByProjectId(projectId, updatedBy);
    }

    @Override
    public void deactivateTeamLinksByTeamId(UUID teamId, UUID updatedBy) {
        projectTeamJpaRepository.deactivateByTeamId(teamId, updatedBy);
    }
}
