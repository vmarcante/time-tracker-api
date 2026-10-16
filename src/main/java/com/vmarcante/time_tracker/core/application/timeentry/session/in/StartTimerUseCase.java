package com.vmarcante.time_tracker.core.application.timeentry.session.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.session.dto.input.StartTimerInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.TimeEntryDetailOutputDTO;

public interface StartTimerUseCase {

    TimeEntryDetailOutputDTO execute(StartTimerInputDTO input) throws ApplicationException;
}
