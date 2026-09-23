package com.vmarcante.time_tracker.core.application.project.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.input.CreateProjectInputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.CreateProjectOutputDTO;

public interface CreatePersonalProjectUseCase {

    CreateProjectOutputDTO execute(CreateProjectInputDTO input) throws ApplicationException;
}
