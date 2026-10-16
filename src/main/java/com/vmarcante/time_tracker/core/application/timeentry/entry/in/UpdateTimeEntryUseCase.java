package com.vmarcante.time_tracker.core.application.timeentry.entry.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.input.UpdateTimeEntryInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.TimeEntryDetailOutputDTO;

public interface UpdateTimeEntryUseCase {

    TimeEntryDetailOutputDTO execute(UUID entryId, UpdateTimeEntryInputDTO input) throws ApplicationException;
}
