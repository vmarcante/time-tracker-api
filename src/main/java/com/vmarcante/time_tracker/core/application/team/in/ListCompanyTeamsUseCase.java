package com.vmarcante.time_tracker.core.application.team.in;

import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.input.TeamFilterInputDTO;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamSummaryOutputDTO;

public interface ListCompanyTeamsUseCase {

    PageWrapperDTO<TeamSummaryOutputDTO> execute(UUID companyId, TeamFilterInputDTO filter)
            throws ApplicationException;
}
