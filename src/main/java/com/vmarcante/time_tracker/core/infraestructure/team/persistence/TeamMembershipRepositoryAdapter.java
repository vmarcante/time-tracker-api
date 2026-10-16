package com.vmarcante.time_tracker.core.infraestructure.team.persistence;

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
import com.vmarcante.time_tracker.core.domain.team.repository.TeamMembershipRepository;
import com.vmarcante.time_tracker.core.infraestructure.team.mapper.TeamPersistenceMapper;

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
    public boolean isTeamMember(UUID userId, UUID teamId) {
        return teamMembershipJpaRepository
                .existsByUserIdAndTeamIdAndActiveTrueAndApprovedTrue(userId, teamId);
    }

    @Override
    public boolean isTeamLead(UUID userId, UUID teamId) {
        return teamMembershipJpaRepository
                .existsByUserIdAndTeamIdAndRoleAndActiveTrueAndApprovedTrue(
                        userId, teamId, TeamRole.LEAD);
    }

    @Override
    public boolean hasAnyActiveMembership(UUID userId, UUID teamId) {
        return teamMembershipJpaRepository.existsByUserIdAndTeamIdAndActiveTrue(userId, teamId);
    }

    @Override
    public Optional<TeamMembership> findMembership(UUID userId, UUID teamId) {
        return teamMembershipJpaRepository
                .findByUserIdAndTeamIdAndActiveTrueAndApprovedTrue(userId, teamId)
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public Optional<TeamMembership> findMembershipById(UUID membershipId) {
        return teamMembershipJpaRepository.findById(membershipId)
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public Optional<TeamMembership> findLeadMembership(UUID teamId) {
        return teamMembershipJpaRepository
                .findByTeamIdAndRoleAndActiveTrueAndApprovedTrue(teamId, TeamRole.LEAD)
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public Page<TeamMembership> findApprovedMembershipsByTeamId(UUID teamId, Pageable pageable) {
        return teamMembershipJpaRepository.findByTeamIdAndActiveTrueAndApprovedTrue(teamId, pageable)
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public List<TeamMembership> findApprovedMembershipsByTeamIds(Collection<UUID> teamIds) {
        if (teamIds == null || teamIds.isEmpty()) {
            return List.of();
        }
        return teamMembershipJpaRepository.findByTeamIdInAndActiveTrueAndApprovedTrue(teamIds).stream()
                .map(TeamPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<TeamMembership> findActiveMembershipsByUserIdAndCompanyId(UUID userId, UUID companyId) {
        return teamMembershipJpaRepository.findActiveByUserIdAndCompanyId(userId, companyId).stream()
                .map(TeamPersistenceMapper::toDomain)
                .toList();
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
