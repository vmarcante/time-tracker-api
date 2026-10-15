package com.vmarcante.time_tracker.core.application.timeentry.dto.output;

import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.project.model.Project;

public record TimeEntryProjectOutputDTO(
        UUID id,
        String name,
        String clientName) {

    public static TimeEntryProjectOutputDTO from(Project project) {
        return new TimeEntryProjectOutputDTO(project.getId(), project.getName(), project.getClientName());
    }
}
