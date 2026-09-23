package com.vmarcante.time_tracker.core.application.timeentry.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.TagInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TagOutputDTO;

public interface UpdateTagUseCase {

    TagOutputDTO execute(UUID tagId, TagInputDTO input) throws ApplicationException;
}
