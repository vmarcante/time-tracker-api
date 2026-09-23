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
import com.vmarcante.time_tracker.core.domain.team.model.Team;
import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;
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
    public List<TeamMembership> findActiveMembershipsByUserIdAndCompanyId(UUID userId, UUID companyId) {
        return userTeamJpaRepository.findActiveByUserIdAndCompanyId(userId, companyId).stream()
                .map(TeamPersistenceMapper::toDomain)
                .toList();
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
