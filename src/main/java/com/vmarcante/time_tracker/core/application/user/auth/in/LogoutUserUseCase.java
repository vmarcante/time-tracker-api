package com.vmarcante.time_tracker.core.application.user.auth.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface LogoutUserUseCase {
    
    void execute() throws ApplicationException;
}
