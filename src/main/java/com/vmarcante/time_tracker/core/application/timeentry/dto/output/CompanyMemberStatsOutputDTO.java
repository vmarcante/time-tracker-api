package com.vmarcante.time_tracker.core.application.timeentry.dto.output;

import java.util.UUID;

public record CompanyMemberStatsOutputDTO(
        UUID userId,
        String userName,
        long seconds,
        int entryCount) {
}
