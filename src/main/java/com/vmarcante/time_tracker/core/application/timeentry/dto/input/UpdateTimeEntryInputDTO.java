package com.vmarcante.time_tracker.core.application.timeentry.dto.input;

import java.util.UUID;

public record UpdateTimeEntryInputDTO(
        String name,
        String description,
        UUID projectId,
        Boolean clearProject) {
}
