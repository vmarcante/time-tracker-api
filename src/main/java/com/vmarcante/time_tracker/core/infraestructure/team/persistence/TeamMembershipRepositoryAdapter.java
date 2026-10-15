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

    private final UserTeamJpaRepository userTeamJpaRepository;

    public TeamMembershipRepositoryAdapter(UserTeamJpaRepository userTeamJpaRepository) {
        this.userTeamJpaRepository = userTeamJpaRepository;
    }

    @Override
    public TeamMembership saveMembership(TeamMembership membership) {
        UserTeamJpaEntity entity = TeamPersistenceMapper.toEntity(membership);
        return TeamPersistenceMapper.toDomain(userTeamJpaRepository.save(entity));
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
    public Page<TeamMembership> findApprovedMembershipsByTeamId(UUID teamId, Pageable pageable) {
        return userTeamJpaRepository.findByTeamIdAndActiveTrueAndApprovedTrue(teamId, pageable)
                .map(TeamPersistenceMapper::toDomain);
    }

    @Override
    public List<TeamMembership> findApprovedMembershipsByTeamIds(Collection<UUID> teamIds) {
        if (teamIds == null || teamIds.isEmpty()) {
            return List.of();
        }
        return userTeamJpaRepository.findByTeamIdInAndActiveTrueAndApprovedTrue(teamIds).stream()
                .map(TeamPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<TeamMembership> findActiveMembershipsByUserIdAndCompanyId(UUID userId, UUID companyId) {
        return userTeamJpaRepository.findActiveByUserIdAndCompanyId(userId, companyId).stream()
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

    @Override
    public void deactivateMembershipsByUserIdAndCompanyId(UUID userId, UUID companyId, UUID updatedBy) {
        userTeamJpaRepository.deactivateByUserIdAndCompanyId(userId, companyId, updatedBy);
    }

    @Override
    public void demoteLeadsByUserIdAndCompanyId(UUID userId, UUID companyId, UUID updatedBy) {
        userTeamJpaRepository.demoteLeadsByUserIdAndCompanyId(
                userId, companyId, TeamRole.LEAD, TeamRole.MEMBER, updatedBy);
    }
}
