package com.vmarcante.time_tracker.core.application.project.personal.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectDetailOutputDTO;
import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;

public interface ChangePersonalProjectStatusUseCase {

    ProjectDetailOutputDTO execute(UUID projectId, ProjectStatus targetStatus) throws ApplicationException;
}
