package com.vmarcante.time_tracker.core.application.user.auth.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.CreateUserInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.output.CreateUserOutputDTO;

public interface RegisterUserUseCase {

    CreateUserOutputDTO execute(CreateUserInputDTO input) throws ApplicationException;
}
