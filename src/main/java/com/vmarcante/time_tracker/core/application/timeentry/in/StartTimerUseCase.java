package com.vmarcante.time_tracker.core.application.timeentry.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.StartTimerInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntryDetailOutputDTO;

public interface StartTimerUseCase {

    TimeEntryDetailOutputDTO execute(StartTimerInputDTO input) throws ApplicationException;
}
