package com.vmarcante.time_tracker.core.application.project.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface UnassignProjectMemberUseCase {

    void execute(UUID companyId, UUID teamId, UUID projectId, UUID assignmentId)
            throws ApplicationException;
}
