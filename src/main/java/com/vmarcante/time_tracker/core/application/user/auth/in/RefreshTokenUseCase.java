package com.vmarcante.time_tracker.core.application.user.auth.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.RefreshTokenInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.output.AuthenticationOutputDTO;

public interface RefreshTokenUseCase {
    
    AuthenticationOutputDTO execute(RefreshTokenInputDTO input) throws ApplicationException;
}
