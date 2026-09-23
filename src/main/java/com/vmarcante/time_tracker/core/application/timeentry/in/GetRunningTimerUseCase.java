package com.vmarcante.time_tracker.core.application.timeentry.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.RunningTimerOutputDTO;

public interface GetRunningTimerUseCase {

    RunningTimerOutputDTO execute() throws ApplicationException;
}
