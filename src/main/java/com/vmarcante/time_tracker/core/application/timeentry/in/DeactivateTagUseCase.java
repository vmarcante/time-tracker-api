package com.vmarcante.time_tracker.core.application.timeentry.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface DeactivateTagUseCase {

    void execute(UUID tagId) throws ApplicationException;
}
