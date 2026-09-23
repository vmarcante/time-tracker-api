package com.vmarcante.time_tracker.core.application.project.in;

import java.util.List;
import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectAssignmentOutputDTO;

public interface GetMemberRemovalImpactUseCase {

    List<ProjectAssignmentOutputDTO> execute(UUID companyId, UUID teamId, UUID membershipId)
            throws ApplicationException;
}
