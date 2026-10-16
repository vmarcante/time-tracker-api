package com.vmarcante.time_tracker.core.application.timeentry.dto.output;

import java.time.LocalDate;

public record CompanyDayStatsOutputDTO(
        LocalDate date,
        long seconds) {
}
