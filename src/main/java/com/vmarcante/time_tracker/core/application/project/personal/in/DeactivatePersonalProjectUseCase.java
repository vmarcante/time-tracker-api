package com.vmarcante.time_tracker.core.application.project.personal.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface DeactivatePersonalProjectUseCase {

    void execute(UUID projectId) throws ApplicationException;
}
