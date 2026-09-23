package com.vmarcante.time_tracker.core.application.team.dto.output;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.team.model.Team;

public record CreateTeamOutputDTO(
        UUID id,
        UUID companyId,
        String name,
        String description,
        LocalDateTime createdAt) {

    public static CreateTeamOutputDTO from(Team team) {
        return new CreateTeamOutputDTO(
                team.getId(),
                team.getCompanyId(),
                team.getName(),
                team.getDescription(),
                team.getCreatedAt());
    }
}
