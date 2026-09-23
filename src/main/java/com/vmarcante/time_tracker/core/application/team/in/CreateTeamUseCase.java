package com.vmarcante.time_tracker.core.application.team.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.input.CreateTeamInputDTO;
import com.vmarcante.time_tracker.core.application.team.dto.output.CreateTeamOutputDTO;

public interface CreateTeamUseCase {

    CreateTeamOutputDTO execute(UUID companyId, CreateTeamInputDTO input) throws ApplicationException;
}
