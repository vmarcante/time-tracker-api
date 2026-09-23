package com.vmarcante.time_tracker.core.application.project.dto.output;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;
import com.vmarcante.time_tracker.core.domain.project.model.Project;

public record CreateProjectOutputDTO(
        UUID id,
        UUID companyId,
        String name,
        String description,
        ProjectStatus status,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime createdAt) {

    public static CreateProjectOutputDTO from(Project project) {
        return new CreateProjectOutputDTO(
                project.getId(),
                project.getCompanyId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getStartDate(),
                project.getEndDate(),
                project.getCreatedAt());
    }
}
