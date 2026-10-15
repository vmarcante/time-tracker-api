package com.vmarcante.time_tracker.core.application.project.dto.input;

import java.time.LocalDate;
import java.util.List;

public record UpdateProjectInputDTO(
        String name,
        String clientName,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        List<ProjectTeamMembersInputDTO> teams) {
}
