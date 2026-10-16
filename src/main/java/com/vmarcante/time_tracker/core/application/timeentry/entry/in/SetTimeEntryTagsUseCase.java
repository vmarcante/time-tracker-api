package com.vmarcante.time_tracker.core.application.timeentry.entry.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.input.SetTimeEntryTagsInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.TimeEntryDetailOutputDTO;

public interface SetTimeEntryTagsUseCase {

    TimeEntryDetailOutputDTO execute(UUID entryId, SetTimeEntryTagsInputDTO input) throws ApplicationException;
}
