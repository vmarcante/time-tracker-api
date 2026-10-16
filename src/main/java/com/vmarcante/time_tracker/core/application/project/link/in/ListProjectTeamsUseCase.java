package com.vmarcante.time_tracker.core.application.project.link.in;

import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.input.TeamFilterInputDTO;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamSummaryOutputDTO;

public interface ListProjectTeamsUseCase {

    PageWrapperDTO<TeamSummaryOutputDTO> execute(UUID companyId, UUID projectId, TeamFilterInputDTO filter)
            throws ApplicationException;
}
