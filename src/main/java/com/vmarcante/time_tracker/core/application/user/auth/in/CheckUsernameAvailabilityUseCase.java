package com.vmarcante.time_tracker.core.application.user.auth.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface CheckUsernameAvailabilityUseCase {
    boolean execute(String username) throws ApplicationException;
}
