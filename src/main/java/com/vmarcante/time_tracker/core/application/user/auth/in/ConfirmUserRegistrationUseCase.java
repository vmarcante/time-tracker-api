package com.vmarcante.time_tracker.core.application.user.auth.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.ConfirmUserRegistrationInputDTO;

public interface ConfirmUserRegistrationUseCase {
    
    void execute(ConfirmUserRegistrationInputDTO input) throws ApplicationException;
}
