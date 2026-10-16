package com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output;

import java.util.List;

public record CompanyTimeEntryStatsOutputDTO(
        long totalSeconds,
        int entryCount,
        int runningCount,
        int memberCount,
        long previousTotalSeconds,
        int previousEntryCount,
        List<CompanyMemberStatsOutputDTO> members,
        List<CompanyProjectStatsOutputDTO> projects,
        List<CompanyDayStatsOutputDTO> days,
        List<CompanyProjectDayStatsOutputDTO> projectDays,
        List<CompanyRunningMemberOutputDTO> running) {
}
