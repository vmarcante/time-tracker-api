package com.vmarcante.time_tracker.core.application.team.dto.output;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.team.enums.TeamRole;
import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;

public record TeamMemberOutputDTO(
        UUID membershipId,
        UUID userId,
        String name,
        TeamRole role,
        LocalDateTime createdAt) {

    public static TeamMemberOutputDTO from(TeamMembership membership, String memberName) {
        return new TeamMemberOutputDTO(
                membership.getId(),
                membership.getUserId(),
                memberName,
                membership.getRole(),
                membership.getCreatedAt());
    }
}
