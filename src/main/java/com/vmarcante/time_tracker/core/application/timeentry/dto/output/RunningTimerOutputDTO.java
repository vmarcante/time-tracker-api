package com.vmarcante.time_tracker.core.application.timeentry.dto.output;

import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;

public record RunningTimerOutputDTO(
        UUID entryId,
        String entryName,
        TimeEntrySessionOutputDTO session) {

    public static RunningTimerOutputDTO from(TimeEntry entry, TimeEntrySession session) {
        return new RunningTimerOutputDTO(
                entry.getId(),
                entry.getName(),
                TimeEntrySessionOutputDTO.from(session));
    }
}
