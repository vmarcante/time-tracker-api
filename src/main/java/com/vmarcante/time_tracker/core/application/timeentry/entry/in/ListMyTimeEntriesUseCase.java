package com.vmarcante.time_tracker.core.application.timeentry.entry.in;

import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.input.MyTimeEntriesFilterInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.TimeEntrySummaryOutputDTO;

public interface ListMyTimeEntriesUseCase {

    PageWrapperDTO<TimeEntrySummaryOutputDTO> execute(MyTimeEntriesFilterInputDTO filter)
            throws ApplicationException;
}
