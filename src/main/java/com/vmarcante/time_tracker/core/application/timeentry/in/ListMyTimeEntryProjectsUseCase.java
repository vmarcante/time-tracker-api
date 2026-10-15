package com.vmarcante.time_tracker.core.application.timeentry.in;

import java.util.List;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntryProjectOutputDTO;

public interface ListMyTimeEntryProjectsUseCase {

    List<TimeEntryProjectOutputDTO> execute() throws ApplicationException;
}
