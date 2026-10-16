package com.vmarcante.time_tracker.core.application.timeentry.entry.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.input.CreateTimeEntryInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.TimeEntryDetailOutputDTO;

public interface CreateTimeEntryUseCase {

    TimeEntryDetailOutputDTO execute(CreateTimeEntryInputDTO input) throws ApplicationException;
}
