package com.vmarcante.time_tracker.core.application.timeentry.in;

import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.CompanyTimeEntriesFilterInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntrySummaryOutputDTO;

public interface ListCompanyTimeEntriesUseCase {

    PageWrapperDTO<TimeEntrySummaryOutputDTO> execute(UUID companyId, CompanyTimeEntriesFilterInputDTO filter)
            throws ApplicationException;
}
