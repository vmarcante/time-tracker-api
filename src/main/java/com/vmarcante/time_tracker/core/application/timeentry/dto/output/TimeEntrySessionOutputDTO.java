package com.vmarcante.time_tracker.core.application.timeentry.dto.output;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;

public record TimeEntrySessionOutputDTO(
        UUID id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String description,
        boolean running,
        long durationSeconds) {

    public static TimeEntrySessionOutputDTO from(TimeEntrySession session) {
        LocalDateTime effectiveEnd = session.getEndTime() != null ? session.getEndTime() : LocalDateTime.now();
        long seconds = Math.max(0, Duration.between(session.getStartTime(), effectiveEnd).getSeconds());
        return new TimeEntrySessionOutputDTO(
                session.getId(),
                session.getStartTime(),
                session.getEndTime(),
                session.getDescription(),
                session.getEndTime() == null,
                seconds);
    }
}
