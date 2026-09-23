package com.vmarcante.time_tracker.core.application.project.dto.input;

import java.time.LocalDate;

public record UpdateProjectInputDTO(
        String name,
        String clientName,
        String description,
        LocalDate startDate,
        LocalDate endDate) {
}
