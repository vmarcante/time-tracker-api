package com.vmarcante.time_tracker.core.application.project.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.input.CreateProjectInputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.CreateProjectOutputDTO;

public interface CreateProjectUseCase {

    CreateProjectOutputDTO execute(UUID companyId, CreateProjectInputDTO input) throws ApplicationException;
}
