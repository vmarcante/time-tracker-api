package com.vmarcante.time_tracker.core.application.user.auth.dto;

import java.util.UUID;

public record CreateUserAuthDTO(
        UUID personId,
        String username,
        String password) {

}
