package com.vmarcante.time_tracker.core.application.team.membership.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.membership.dto.input.TeamMemberEmailInputDTO;
import com.vmarcante.time_tracker.core.application.team.membership.dto.output.TeamMemberOutputDTO;

public interface AssignTeamLeadUseCase {

    TeamMemberOutputDTO execute(UUID companyId, UUID teamId, TeamMemberEmailInputDTO input)
            throws ApplicationException;
}
