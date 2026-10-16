package com.vmarcante.time_tracker.core.application.timeentry.in;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.CompanyTimeEntryStatsOutputDTO;

public interface ListCompanyTimeEntryStatsUseCase {

    CompanyTimeEntryStatsOutputDTO execute(UUID companyId, List<UUID> userIds, UUID projectId, List<UUID> teamIds,
            LocalDateTime from, LocalDateTime to) throws ApplicationException;
}
