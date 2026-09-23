package com.vmarcante.time_tracker.core.application.timeentry.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.UpdateTimeEntryInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntryDetailOutputDTO;

public interface UpdateTimeEntryUseCase {

    TimeEntryDetailOutputDTO execute(UUID entryId, UpdateTimeEntryInputDTO input) throws ApplicationException;
}
