package com.vmarcante.time_tracker.core.application.project.in;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamSummaryOutputDTO;

public interface ListProjectTeamsUseCase {

    Page<TeamSummaryOutputDTO> execute(UUID companyId, UUID projectId, Pageable pageable)
            throws ApplicationException;
}
