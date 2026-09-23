package com.vmarcante.time_tracker.core.application.project.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.input.ProjectMemberEmailInputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectAssignmentOutputDTO;

public interface AssignProjectMemberUseCase {

    ProjectAssignmentOutputDTO execute(
            UUID companyId, UUID teamId, UUID projectId, ProjectMemberEmailInputDTO input)
            throws ApplicationException;
}
