package com.vmarcante.time_tracker.core.application.timeentry.dto.output;

import java.util.UUID;

public record CompanyProjectStatsOutputDTO(
        UUID projectId,
        String projectName,
        String clientName,
        long seconds,
        int entryCount) {
}
