package com.vmarcante.time_tracker.core.infrastructure.team.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.team.enums.TeamRole;
import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamMembershipFilter;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamMembershipRepository;
import com.vmarcante.time_tracker.core.infrastructure.team.mapper.TeamPersistenceMapper;

@Component
public class TeamMembershipRepositoryAdapter implements TeamMembershipRepository {

    private final TeamMembershipJpaRepository teamMembershipJpaRepository;

    public TeamMembershipRepositoryAdapter(TeamMembershipJpaRepository teamMembershipJpaRepository) {
        this.teamMembershipJpaRepository = teamMembershipJpaRepository;
    }

    @Override
    public TeamMembership saveMembership(TeamMembership membership) {
        TeamMembershipJpaEntity entity = TeamPersistenceMapper.toEntity(membership);
        return TeamPersistenceMapper.toDomain(teamMembershipJpaRepository.save(entity));
    }

    @Override
    public Optional<TeamMembership> findById(UUID membershipId) {
        return teamMembershipJpaRepository.findById(membershipId)
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public Optional<TeamMembership> findOne(TeamMembershipFilter filter) {
        return teamMembershipJpaRepository
                .findOne(TeamMembershipRepositorySpecification.withFilter(filter))
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public List<TeamMembership> search(TeamMembershipFilter filter) {
        return teamMembershipJpaRepository
                .findAll(TeamMembershipRepositorySpecification.withFilter(filter))
                .stream()
                .map(TeamPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Page<TeamMembership> search(TeamMembershipFilter filter, Pageable pageable) {
        return teamMembershipJpaRepository
                .findAll(TeamMembershipRepositorySpecification.withFilter(filter), pageable)
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public boolean exists(TeamMembershipFilter filter) {
        return teamMembershipJpaRepository
                .exists(TeamMembershipRepositorySpecification.withFilter(filter));
    }

    @Override
    public Map<UUID, Long> countApprovedMembersByTeamIds(Collection<UUID> teamIds) {
        return teamMembershipJpaRepository.countApprovedMembersByTeamIds(teamIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]));
    }

    @Override
    public Map<UUID, UUID> findLeadUserIdsByTeamIds(Collection<UUID> teamIds) {
        return teamMembershipJpaRepository.findLeadUserIdsByTeamIds(teamIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (UUID) row[1]));
    }

    @Override
    public List<UUID> findApprovedTeamIdsByUserIdAndCompanyId(UUID userId, UUID companyId) {
        return teamMembershipJpaRepository.findApprovedTeamIdsByUserIdAndCompanyId(userId, companyId);
    }

    @Override
    public void deactivateMembershipsByTeamId(UUID teamId, UUID updatedBy) {
        teamMembershipJpaRepository.deactivateByTeamId(teamId, updatedBy);
    }

    @Override
    public void deactivateMembershipsByUserIdAndCompanyId(UUID userId, UUID companyId, UUID updatedBy) {
        teamMembershipJpaRepository.deactivateByUserIdAndCompanyId(userId, companyId, updatedBy);
    }

    @Override
    public void demoteLeadsByUserIdAndCompanyId(UUID userId, UUID companyId, UUID updatedBy) {
        teamMembershipJpaRepository.demoteLeadsByUserIdAndCompanyId(
                userId, companyId, TeamRole.LEAD, TeamRole.MEMBER, updatedBy);
    }
}
