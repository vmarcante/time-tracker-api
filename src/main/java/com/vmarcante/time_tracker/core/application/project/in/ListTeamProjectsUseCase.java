package com.vmarcante.time_tracker.core.application.project.in;

import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.input.ProjectFilterInputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectSummaryOutputDTO;

public interface ListTeamProjectsUseCase {

    PageWrapperDTO<ProjectSummaryOutputDTO> execute(UUID companyId, UUID teamId, ProjectFilterInputDTO filter)
            throws ApplicationException;
}
