package com.vmarcante.time_tracker.core.domain.team.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;

public interface TeamMembershipRepository {

    TeamMembership saveMembership(TeamMembership membership);

    boolean isTeamMember(UUID userId, UUID teamId);

    boolean isTeamLead(UUID userId, UUID teamId);

    boolean hasAnyActiveMembership(UUID userId, UUID teamId);

    Optional<TeamMembership> findMembership(UUID userId, UUID teamId);

    Optional<TeamMembership> findMembershipById(UUID membershipId);

    Optional<TeamMembership> findLeadMembership(UUID teamId);

    Page<TeamMembership> findApprovedMembershipsByTeamId(UUID teamId, Pageable pageable);

    List<TeamMembership> findApprovedMembershipsByTeamIds(Collection<UUID> teamIds);

    List<TeamMembership> findActiveMembershipsByUserIdAndCompanyId(UUID userId, UUID companyId);

    Map<UUID, Long> countApprovedMembersByTeamIds(Collection<UUID> teamIds);

    Map<UUID, UUID> findLeadUserIdsByTeamIds(Collection<UUID> teamIds);

    void deactivateMembershipsByTeamId(UUID teamId, UUID updatedBy);

    void deactivateMembershipsByUserIdAndCompanyId(UUID userId, UUID companyId, UUID updatedBy);

    void demoteLeadsByUserIdAndCompanyId(UUID userId, UUID companyId, UUID updatedBy);
}
