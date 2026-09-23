package com.vmarcante.time_tracker.core.application.project.in;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectAssignmentOutputDTO;

public interface ListProjectMembersUseCase {

    Page<ProjectAssignmentOutputDTO> execute(UUID companyId, UUID projectId, Pageable pageable)
            throws ApplicationException;
}
