package com.vmarcante.time_tracker.core.application.timeentry.session.dto.input;

import java.time.LocalDateTime;

public record SessionInputDTO(
        LocalDateTime startTime,
        LocalDateTime endTime,
        String description) {
}
