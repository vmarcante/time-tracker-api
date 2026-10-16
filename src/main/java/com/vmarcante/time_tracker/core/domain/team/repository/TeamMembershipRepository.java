package com.vmarcante.time_tracker.core.domain.team.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamMembershipFilter;

public interface TeamMembershipRepository {

    TeamMembership saveMembership(TeamMembership membership);

    Optional<TeamMembership> findById(UUID membershipId);

    Optional<TeamMembership> findOne(TeamMembershipFilter filter);

    List<TeamMembership> search(TeamMembershipFilter filter);

    Page<TeamMembership> search(TeamMembershipFilter filter, Pageable pageable);

    boolean exists(TeamMembershipFilter filter);

    Map<UUID, Long> countApprovedMembersByTeamIds(Collection<UUID> teamIds);

    Map<UUID, UUID> findLeadUserIdsByTeamIds(Collection<UUID> teamIds);

    List<UUID> findApprovedTeamIdsByUserIdAndCompanyId(UUID userId, UUID companyId);

    void deactivateMembershipsByTeamId(UUID teamId, UUID updatedBy);

    void deactivateMembershipsByUserIdAndCompanyId(UUID userId, UUID companyId, UUID updatedBy);

    void demoteLeadsByUserIdAndCompanyId(UUID userId, UUID companyId, UUID updatedBy);
}
