package com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output;

import java.time.LocalDate;
import java.util.UUID;

public record CompanyProjectDayStatsOutputDTO(
        UUID projectId,
        LocalDate date,
        long seconds) {
}
