package com.vmarcante.time_tracker.core.application.user.auth.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.CreateUserAuthDTO;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;

public interface CreateUserUseCase {
    UserAuth execute(CreateUserAuthDTO input) throws ApplicationException;
}
