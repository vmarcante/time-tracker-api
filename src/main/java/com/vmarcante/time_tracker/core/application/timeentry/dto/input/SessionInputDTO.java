package com.vmarcante.time_tracker.core.application.timeentry.dto.input;

import java.time.LocalDateTime;

public record SessionInputDTO(
        LocalDateTime startTime,
        LocalDateTime endTime,
        String description) {
}
