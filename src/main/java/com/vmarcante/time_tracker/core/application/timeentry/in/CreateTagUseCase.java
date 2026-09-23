package com.vmarcante.time_tracker.core.application.timeentry.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.TagInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TagOutputDTO;

public interface CreateTagUseCase {

    TagOutputDTO execute(TagInputDTO input) throws ApplicationException;
}
