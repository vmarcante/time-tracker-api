package com.vmarcante.time_tracker.core.application.project.dto.output;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;
import com.vmarcante.time_tracker.core.domain.project.model.Project;

public record ProjectDetailOutputDTO(
        UUID id,
        UUID companyId,
        String name,
        String clientName,
        String description,
        ProjectStatus status,
        LocalDate startDate,
        LocalDate endDate,
        long teamCount,
        long memberCount,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static ProjectDetailOutputDTO from(Project project, long teamCount, long memberCount) {
        return new ProjectDetailOutputDTO(
                project.getId(),
                project.getCompanyId(),
                project.getName(),
                project.getClientName(),
                project.getDescription(),
                project.getStatus(),
                project.getStartDate(),
                project.getEndDate(),
                teamCount,
                memberCount,
                project.getActive(),
                project.getCreatedAt(),
                project.getUpdatedAt());
    }
}
