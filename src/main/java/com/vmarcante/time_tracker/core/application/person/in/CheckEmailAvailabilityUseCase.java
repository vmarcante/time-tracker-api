package com.vmarcante.time_tracker.core.application.person.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface CheckEmailAvailabilityUseCase {

    boolean execute(String email) throws ApplicationException;
}
