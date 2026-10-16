package com.vmarcante.time_tracker.core.application.timeentry.dto.output;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompanyRunningMemberOutputDTO(
        UUID userId,
        String userName,
        UUID entryId,
        String entryName,
        LocalDateTime startedAt) {
}
