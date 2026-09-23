package com.vmarcante.time_tracker.core.application.project.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectDetailOutputDTO;

public interface FindPersonalProjectByIdUseCase {

    ProjectDetailOutputDTO execute(UUID projectId) throws ApplicationException;
}
