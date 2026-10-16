package com.vmarcante.time_tracker.core.application.project.link.in;

import java.util.List;
import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.member.dto.output.ProjectAssignmentOutputDTO;

public interface GetUnlinkImpactUseCase {

    List<ProjectAssignmentOutputDTO> execute(UUID companyId, UUID projectId, UUID teamId)
            throws ApplicationException;
}
