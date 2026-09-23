package com.vmarcante.time_tracker.core.application.timeentry.dto.output;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.timeentry.model.Tag;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;

public record TimeEntryDetailOutputDTO(
        UUID id,
        UUID userId,
        String userName,
        String name,
        String description,
        UUID projectId,
        String projectName,
        String clientName,
        UUID companyId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        long durationSeconds,
        boolean running,
        List<TagOutputDTO> tags,
        List<TimeEntrySessionOutputDTO> sessions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static TimeEntryDetailOutputDTO from(TimeEntry entry, List<TimeEntrySession> sessions,
            List<Tag> tags, Project project, String userName) {
        TimeEntrySummaryOutputDTO summary = TimeEntrySummaryOutputDTO.from(entry, sessions, tags, project, userName);
        return new TimeEntryDetailOutputDTO(
                entry.getId(),
                entry.getUserId(),
                userName,
                entry.getName(),
                entry.getDescription(),
                project.getId(),
                project.getName(),
                project.getClientName(),
                entry.getCompanyId(),
                summary.startTime(),
                summary.endTime(),
                summary.durationSeconds(),
                summary.running(),
                summary.tags(),
                sessions.stream().map(TimeEntrySessionOutputDTO::from).toList(),
                entry.getCreatedAt(),
                entry.getUpdatedAt());
    }
}
