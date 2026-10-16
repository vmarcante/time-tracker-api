package com.vmarcante.time_tracker.core.application.project.dto.input;

import java.time.LocalDate;
import java.util.List;

import com.vmarcante.time_tracker.core.application.project.link.dto.input.ProjectTeamMembersInputDTO;

public record CreateProjectInputDTO(
        String name,
        String clientName,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        List<ProjectTeamMembersInputDTO> teams) {
}
