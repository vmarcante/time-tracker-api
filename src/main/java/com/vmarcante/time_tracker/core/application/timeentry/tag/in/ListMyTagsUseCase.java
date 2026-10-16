package com.vmarcante.time_tracker.core.application.timeentry.tag.in;

import java.util.List;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.tag.dto.output.TagOutputDTO;

public interface ListMyTagsUseCase {

    List<TagOutputDTO> execute() throws ApplicationException;
}
