package com.vmarcante.time_tracker.core.application.timeentry.session.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.session.dto.output.RunningTimerOutputDTO;

public interface GetRunningTimerUseCase {

    RunningTimerOutputDTO execute() throws ApplicationException;
}
