package com.vmarcante.time_tracker.core.application.timeentry.tag.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.tag.dto.input.TagInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.tag.dto.output.TagOutputDTO;

public interface CreateTagUseCase {

    TagOutputDTO execute(TagInputDTO input) throws ApplicationException;
}
