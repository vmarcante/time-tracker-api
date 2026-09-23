package com.vmarcante.time_tracker.core.application.team.in;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamMemberOutputDTO;

public interface ListTeamMembersUseCase {

    Page<TeamMemberOutputDTO> execute(UUID companyId, UUID teamId, Pageable pageable)
            throws ApplicationException;
}
