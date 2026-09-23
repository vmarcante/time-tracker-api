package com.vmarcante.time_tracker.core.application.team.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.input.UpdateTeamInputDTO;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamDetailOutputDTO;

public interface UpdateTeamUseCase {

    TeamDetailOutputDTO execute(UUID companyId, UUID teamId, UpdateTeamInputDTO input)
            throws ApplicationException;
}
