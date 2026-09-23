package com.vmarcante.time_tracker.core.application.timeentry.dto.input;

import java.util.UUID;

public record StartTimerInputDTO(
        UUID entryId,
        UUID projectId,
        String name,
        String description) {
}
