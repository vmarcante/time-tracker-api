package com.vmarcante.time_tracker.core.application.team.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.input.TeamMemberEmailInputDTO;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamMemberOutputDTO;

public interface AddTeamMemberUseCase {

    TeamMemberOutputDTO execute(UUID companyId, UUID teamId, TeamMemberEmailInputDTO input)
            throws ApplicationException;
}
