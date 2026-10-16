package com.vmarcante.time_tracker.core.application.team.membership.in;

import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.company.membership.dto.input.MembershipFilterInputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.membership.dto.output.TeamMemberOutputDTO;

public interface ListTeamMembersUseCase {

    PageWrapperDTO<TeamMemberOutputDTO> execute(UUID companyId, UUID teamId, MembershipFilterInputDTO filter)
            throws ApplicationException;
}
