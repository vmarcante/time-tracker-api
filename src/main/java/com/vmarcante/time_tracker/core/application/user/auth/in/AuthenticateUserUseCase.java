package com.vmarcante.time_tracker.core.application.user.auth.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.UserLoginInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.output.AuthenticationOutputDTO;

public interface AuthenticateUserUseCase {
    AuthenticationOutputDTO execute(UserLoginInputDTO input) throws ApplicationException;

    AuthenticationOutputDTO execute(UserLoginInputDTO input, String deviceInfo, String ipAddress, String userAgent)
            throws ApplicationException;

}
