package com.vmarcante.time_tracker.core.application.user.auth.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.output.CurrentUserOutputDTO;

public interface GetCurrentUserDataUseCase {

    CurrentUserOutputDTO execute() throws ApplicationException;
}
