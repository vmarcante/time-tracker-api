package com.vmarcante.time_tracker.core.application.team.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamDetailOutputDTO;

public interface FindTeamByIdUseCase {

    TeamDetailOutputDTO execute(UUID companyId, UUID teamId) throws ApplicationException;
}
