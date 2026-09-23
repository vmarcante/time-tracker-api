package com.vmarcante.time_tracker.core.application.timeentry.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.CreateTimeEntryInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntryDetailOutputDTO;

public interface CreateTimeEntryUseCase {

    TimeEntryDetailOutputDTO execute(CreateTimeEntryInputDTO input) throws ApplicationException;
}
