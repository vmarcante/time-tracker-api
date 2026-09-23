package com.vmarcante.time_tracker.core.domain.team.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.domain.team.model.Team;
import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;

public interface TeamRepository {

    Team save(Team team);

    TeamMembership saveMembership(TeamMembership membership);

    Optional<Team> findById(UUID id);

    Page<Team> findActiveByCompanyId(UUID companyId, Pageable pageable);

    Page<Team> findActiveTeamsByUserId(UUID userId, UUID companyId, Pageable pageable);

    Page<Team> findActiveByProjectId(UUID projectId, Pageable pageable);

    boolean existsActiveByIdAndCompanyId(UUID teamId, UUID companyId);

    boolean existsActiveByCompanyIdAndName(UUID companyId, String name);

    boolean isTeamMember(UUID userId, UUID teamId);

    boolean isTeamLead(UUID userId, UUID teamId);

    boolean hasAnyActiveMembership(UUID userId, UUID teamId);

    Optional<TeamMembership> findMembership(UUID userId, UUID teamId);

    Optional<TeamMembership> findMembershipById(UUID membershipId);

    Optional<TeamMembership> findLeadMembership(UUID teamId);

    Page<TeamMembership> findApprovedMembershipsByTeamId(UUID teamId, Pageable pageable);

    List<TeamMembership> findActiveMembershipsByUserIdAndCompanyId(UUID userId, UUID companyId);

    Map<UUID, Long> countApprovedMembersByTeamIds(Collection<UUID> teamIds);

    Map<UUID, UUID> findLeadUserIdsByTeamIds(Collection<UUID> teamIds);

    void deactivateMembershipsByTeamId(UUID teamId, UUID updatedBy);

    void deactivateMembershipsByUserIdAndCompanyId(UUID userId, UUID companyId, UUID updatedBy);

    void demoteLeadsByUserIdAndCompanyId(UUID userId, UUID companyId, UUID updatedBy);
}
