package com.vmarcante.time_tracker.core.application.project.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface DeactivateProjectUseCase {

    void execute(UUID companyId, UUID projectId) throws ApplicationException;
}
