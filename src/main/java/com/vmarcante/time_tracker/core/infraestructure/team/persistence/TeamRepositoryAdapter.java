package com.vmarcante.time_tracker.core.infraestructure.team.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.team.model.Team;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.infraestructure.project.persistence.ProjectTeamJpaRepository;
import com.vmarcante.time_tracker.core.infraestructure.team.mapper.TeamPersistenceMapper;

@Component
public class TeamRepositoryAdapter implements TeamRepository {

    private final TeamJpaRepository teamJpaRepository;
    private final UserTeamJpaRepository userTeamJpaRepository;
    private final ProjectTeamJpaRepository projectTeamJpaRepository;

    public TeamRepositoryAdapter(
            TeamJpaRepository teamJpaRepository,
            UserTeamJpaRepository userTeamJpaRepository,
            ProjectTeamJpaRepository projectTeamJpaRepository) {
        this.teamJpaRepository = teamJpaRepository;
        this.userTeamJpaRepository = userTeamJpaRepository;
        this.projectTeamJpaRepository = projectTeamJpaRepository;
    }

    @Override
    public Team save(Team team) {
        TeamJpaEntity entity = TeamPersistenceMapper.toEntity(team);
        return TeamPersistenceMapper.toDomain(teamJpaRepository.save(entity));
    }

    @Override
    public Optional<Team> findById(UUID id) {
        return teamJpaRepository.findById(id)
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public List<Team> findAllByIds(Collection<UUID> teamIds) {
        if (teamIds == null || teamIds.isEmpty()) {
            return List.of();
        }
        return teamJpaRepository.findAllById(teamIds).stream()
                .map(TeamPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Page<Team> findActiveByCompanyId(UUID companyId, Pageable pageable) {
        return teamJpaRepository.findByCompanyIdAndActiveTrue(companyId, pageable)
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public Page<Team> findActiveTeamsByUserId(UUID userId, UUID companyId, Pageable pageable) {
        return userTeamJpaRepository
                .findActiveTeamsByUserIdAndCompanyId(userId, companyId, pageable)
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public Page<Team> findActiveByProjectId(UUID projectId, Pageable pageable) {
        return projectTeamJpaRepository.findActiveTeamsByProjectId(projectId, pageable)
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsActiveByIdAndCompanyId(UUID teamId, UUID companyId) {
        return teamJpaRepository.existsByIdAndCompanyIdAndActiveTrue(teamId, companyId);
    }

    @Override
    public boolean existsActiveByCompanyIdAndName(UUID companyId, String name) {
        return teamJpaRepository.existsByCompanyIdAndNameIgnoreCaseAndActiveTrue(companyId, name);
    }
}
