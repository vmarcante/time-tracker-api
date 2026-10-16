package com.vmarcante.time_tracker.core.application.timeentry.entry.in;

import java.util.List;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.input.MyTimeEntryHeatmapInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.TimeEntryDayStatsOutputDTO;

public interface GetMyTimeEntryHeatmapUseCase {

    List<TimeEntryDayStatsOutputDTO> execute(MyTimeEntryHeatmapInputDTO input) throws ApplicationException;
}
