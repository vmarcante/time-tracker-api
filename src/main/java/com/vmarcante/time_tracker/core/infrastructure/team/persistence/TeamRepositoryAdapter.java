package com.vmarcante.time_tracker.core.infrastructure.team.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.team.model.Team;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilter;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.infrastructure.team.mapper.TeamPersistenceMapper;

@Component
public class TeamRepositoryAdapter implements TeamRepository {

    private final TeamJpaRepository teamJpaRepository;

    public TeamRepositoryAdapter(TeamJpaRepository teamJpaRepository) {
        this.teamJpaRepository = teamJpaRepository;
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
    public Optional<Team> findOne(TeamFilter filter) {
        return teamJpaRepository
                .findOne(TeamRepositorySpecification.withFilter(filter))
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public List<Team> search(TeamFilter filter) {
        return teamJpaRepository
                .findAll(TeamRepositorySpecification.withFilter(filter))
                .stream()
                .map(TeamPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Page<Team> search(TeamFilter filter, Pageable pageable) {
        return teamJpaRepository
                .findAll(TeamRepositorySpecification.withFilter(filter), pageable)
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public boolean exists(TeamFilter filter) {
        return teamJpaRepository.exists(TeamRepositorySpecification.withFilter(filter));
    }
}
