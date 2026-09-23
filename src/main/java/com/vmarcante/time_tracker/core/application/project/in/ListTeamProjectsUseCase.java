package com.vmarcante.time_tracker.core.application.project.in;

import java.util.List;
import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectSummaryOutputDTO;

public interface ListTeamProjectsUseCase {

    List<ProjectSummaryOutputDTO> execute(UUID companyId, UUID teamId) throws ApplicationException;
}
