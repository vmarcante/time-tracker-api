package com.vmarcante.time_tracker.core.application.user.auth.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.ResetPasswordInputDTO;

public interface ResetPasswordUseCase {
    void execute(ResetPasswordInputDTO input) throws ApplicationException;
}
