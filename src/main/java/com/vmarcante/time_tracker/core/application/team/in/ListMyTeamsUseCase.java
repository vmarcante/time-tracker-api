package com.vmarcante.time_tracker.core.application.team.in;

import java.util.List;
import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamSummaryOutputDTO;

public interface ListMyTeamsUseCase {

    List<TeamSummaryOutputDTO> execute(UUID companyId) throws ApplicationException;
}
