package com.vmarcante.time_tracker.core.application.user.auth.in;

import com.vmarcante.time_tracker.core.application.user.auth.dto.input.RequestPasswordResetInputDTO;

public interface RequestPasswordResetUseCase {
    void execute(RequestPasswordResetInputDTO input);
}
