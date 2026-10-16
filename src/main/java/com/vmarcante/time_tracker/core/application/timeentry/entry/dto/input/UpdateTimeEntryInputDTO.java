package com.vmarcante.time_tracker.core.application.timeentry.entry.dto.input;

import java.util.UUID;

public record UpdateTimeEntryInputDTO(
        String name,
        String description,
        UUID projectId,
        Boolean clearProject) {
}
