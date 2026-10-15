package com.vmarcante.time_tracker.core.application.team.dto.output;

import java.util.List;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.team.model.Team;

public record TeamWithMembersOutputDTO(
        UUID id,
        String name,
        List<TeamMemberOutputDTO> members) {

    public static TeamWithMembersOutputDTO from(Team team, List<TeamMemberOutputDTO> members) {
        return new TeamWithMembersOutputDTO(team.getId(), team.getName(), members);
    }
}
