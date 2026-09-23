package com.vmarcante.time_tracker.core.application.project.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface UnlinkProjectFromTeamUseCase {

    void execute(UUID companyId, UUID projectId, UUID teamId) throws ApplicationException;
}
