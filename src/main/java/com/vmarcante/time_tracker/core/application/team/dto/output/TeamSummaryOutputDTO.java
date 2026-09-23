package com.vmarcante.time_tracker.core.application.team.dto.output;

import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.team.model.Team;

public record TeamSummaryOutputDTO(
        UUID id,
        String name,
        String leadName,
        long memberCount) {

    public static TeamSummaryOutputDTO from(Team team, String leadName, long memberCount) {
        return new TeamSummaryOutputDTO(
                team.getId(),
                team.getName(),
                leadName,
                memberCount);
    }
}
