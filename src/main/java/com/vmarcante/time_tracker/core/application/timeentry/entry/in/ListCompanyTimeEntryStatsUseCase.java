package com.vmarcante.time_tracker.core.application.timeentry.entry.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.input.CompanyTimeEntriesFilterInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.CompanyTimeEntryStatsOutputDTO;

public interface ListCompanyTimeEntryStatsUseCase {

    CompanyTimeEntryStatsOutputDTO execute(UUID companyId, CompanyTimeEntriesFilterInputDTO filter)
            throws ApplicationException;
}
