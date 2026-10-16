package com.vmarcante.time_tracker.core.application.timeentry.tag.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.tag.dto.input.TagInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.tag.dto.output.TagOutputDTO;

public interface UpdateTagUseCase {

    TagOutputDTO execute(UUID tagId, TagInputDTO input) throws ApplicationException;
}
