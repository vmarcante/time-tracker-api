package com.vmarcante.time_tracker.core.infraestructure.team.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.team.enums.TeamRole;
import com.vmarcante.time_tracker.core.domain.team.model.Team;
import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.infraestructure.team.mapper.TeamPersistenceMapper;

@Component
public class TeamRepositoryAdapter implements TeamRepository {

    private final TeamJpaRepository teamJpaRepository;
    private final UserTeamJpaRepository userTeamJpaRepository;

    public TeamRepositoryAdapter(
            TeamJpaRepository teamJpaRepository,
            UserTeamJpaRepository userTeamJpaRepository) {
        this.teamJpaRepository = teamJpaRepository;
        this.userTeamJpaRepository = userTeamJpaRepository;
    }

    @Override
    public Team save(Team team) {
        TeamJpaEntity entity = TeamPersistenceMapper.toEntity(team);
        return TeamPersistenceMapper.toDomain(teamJpaRepository.save(entity));
    }

    @Override
    public TeamMembership saveMembership(TeamMembership membership) {
        UserTeamJpaEntity entity = TeamPersistenceMapper.toEntity(membership);
        return TeamPersistenceMapper.toDomain(userTeamJpaRepository.save(entity));
    }

    @Override
    public Optional<Team> findById(UUID id) {
        return teamJpaRepository.findById(id)
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public List<Team> findActiveByCompanyId(UUID companyId) {
        return teamJpaRepository.findByCompanyIdAndActiveTrue(companyId).stream()
                .map(TeamPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Team> findActiveTeamsByUserId(UUID userId, UUID companyId) {
        List<UUID> teamIds = userTeamJpaRepository
                .findApprovedTeamIdsByUserIdAndCompanyId(userId, companyId);
        return teamJpaRepository.findAllById(teamIds).stream()
                .map(TeamPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsActiveByCompanyIdAndName(UUID companyId, String name) {
        return teamJpaRepository.existsByCompanyIdAndNameIgnoreCaseAndActiveTrue(companyId, name);
    }

    @Override
    public boolean isTeamMember(UUID userId, UUID teamId) {
        return userTeamJpaRepository
                .existsByUserIdAndTeamIdAndActiveTrueAndApprovedTrue(userId, teamId);
    }

    @Override
    public boolean isTeamLead(UUID userId, UUID teamId) {
        return userTeamJpaRepository
                .existsByUserIdAndTeamIdAndRoleAndActiveTrueAndApprovedTrue(
                        userId, teamId, TeamRole.LEAD);
    }

    @Override
    public boolean hasAnyActiveMembership(UUID userId, UUID teamId) {
        return userTeamJpaRepository.existsByUserIdAndTeamIdAndActiveTrue(userId, teamId);
    }

    @Override
    public Optional<TeamMembership> findMembership(UUID userId, UUID teamId) {
        return userTeamJpaRepository
                .findByUserIdAndTeamIdAndActiveTrueAndApprovedTrue(userId, teamId)
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public Optional<TeamMembership> findMembershipById(UUID membershipId) {
        return userTeamJpaRepository.findById(membershipId)
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public Optional<TeamMembership> findLeadMembership(UUID teamId) {
        return userTeamJpaRepository
                .findByTeamIdAndRoleAndActiveTrueAndApprovedTrue(teamId, TeamRole.LEAD)
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public List<TeamMembership> findApprovedMembershipsByTeamId(UUID teamId) {
        return userTeamJpaRepository.findByTeamIdAndActiveTrueAndApprovedTrue(teamId)
                .stream()
                .map(TeamPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Map<UUID, Long> countApprovedMembersByTeamIds(Collection<UUID> teamIds) {
        return userTeamJpaRepository.countApprovedMembersByTeamIds(teamIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]));
    }

    @Override
    public Map<UUID, UUID> findLeadUserIdsByTeamIds(Collection<UUID> teamIds) {
        return userTeamJpaRepository.findLeadUserIdsByTeamIds(teamIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (UUID) row[1]));
    }

    @Override
    public void deactivateMembershipsByTeamId(UUID teamId, UUID updatedBy) {
        userTeamJpaRepository.deactivateByTeamId(teamId, updatedBy);
    }
}
