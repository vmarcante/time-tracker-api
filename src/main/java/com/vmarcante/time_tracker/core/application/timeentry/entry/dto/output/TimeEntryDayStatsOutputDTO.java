package com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output;

import java.time.LocalDate;

public record TimeEntryDayStatsOutputDTO(
        LocalDate date,
        long seconds) {
}
