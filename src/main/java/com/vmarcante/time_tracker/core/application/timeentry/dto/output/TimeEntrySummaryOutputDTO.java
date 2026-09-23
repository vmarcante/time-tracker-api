package com.vmarcante.time_tracker.core.application.timeentry.dto.output;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.timeentry.model.Tag;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;

public record TimeEntrySummaryOutputDTO(
        UUID id,
        UUID userId,
        String userName,
        String name,
        String description,
        UUID projectId,
        String projectName,
        String clientName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        long durationSeconds,
        boolean running,
        List<TagOutputDTO> tags) {

    public static TimeEntrySummaryOutputDTO from(TimeEntry entry, List<TimeEntrySession> sessions,
            List<Tag> tags, Project project, String userName) {
                
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime start = sessions.stream()
                .filter(Objects::nonNull)
                .map(TimeEntrySession::getStartTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);

        LocalDateTime end = sessions.stream()
                .filter(Objects::nonNull)
                .map(TimeEntrySession::getEndTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        boolean running = sessions.stream()
                .filter(Objects::nonNull)
                .anyMatch(s -> s.getEndTime() == null);

        long duration = sessions.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getStartTime() != null)
                .mapToLong(s -> Math.max(0,
                        Duration.between(s.getStartTime(),
                                s.getEndTime() != null ? s.getEndTime() : now).getSeconds()))
                .sum();

        return new TimeEntrySummaryOutputDTO(
                entry.getId(),
                entry.getUserId(),
                userName,
                entry.getName(),
                entry.getDescription(),
                project.getId(),
                project.getName(),
                project.getClientName(),
                start,
                end,
                duration,
                running,
                tags.stream().map(TagOutputDTO::from).toList());
    }
}
