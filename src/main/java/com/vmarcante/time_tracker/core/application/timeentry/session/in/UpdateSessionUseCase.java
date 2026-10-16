package com.vmarcante.time_tracker.core.application.timeentry.session.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.session.dto.input.SessionInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.TimeEntryDetailOutputDTO;

public interface UpdateSessionUseCase {

    TimeEntryDetailOutputDTO execute(UUID entryId, UUID sessionId, SessionInputDTO input)
            throws ApplicationException;
}
