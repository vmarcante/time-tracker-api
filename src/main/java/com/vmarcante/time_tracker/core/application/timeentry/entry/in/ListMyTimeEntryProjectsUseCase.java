package com.vmarcante.time_tracker.core.application.timeentry.entry.in;

import java.util.List;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.TimeEntryProjectOutputDTO;

public interface ListMyTimeEntryProjectsUseCase {

    List<TimeEntryProjectOutputDTO> execute() throws ApplicationException;
}
