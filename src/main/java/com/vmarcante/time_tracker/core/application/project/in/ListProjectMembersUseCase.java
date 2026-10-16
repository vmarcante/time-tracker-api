package com.vmarcante.time_tracker.core.application.project.in;

import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.company.dto.input.MembershipFilterInputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectAssignmentOutputDTO;

public interface ListProjectMembersUseCase {

    PageWrapperDTO<ProjectAssignmentOutputDTO> execute(UUID companyId, UUID projectId,
            MembershipFilterInputDTO filter) throws ApplicationException;
}
