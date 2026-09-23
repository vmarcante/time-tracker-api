package com.vmarcante.time_tracker.core.application.team.dto.output;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.team.model.Team;

public record TeamDetailOutputDTO(
        UUID id,
        UUID companyId,
        String name,
        String description,
        UUID leadUserId,
        String leadName,
        long memberCount,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static TeamDetailOutputDTO from(
            Team team, UUID leadUserId, String leadName, long memberCount) {
        return new TeamDetailOutputDTO(
                team.getId(),
                team.getCompanyId(),
                team.getName(),
                team.getDescription(),
                leadUserId,
                leadName,
                memberCount,
                team.getActive(),
                team.getCreatedAt(),
                team.getUpdatedAt());
    }
}
