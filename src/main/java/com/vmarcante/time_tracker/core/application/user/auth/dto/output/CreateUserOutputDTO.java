package com.vmarcante.time_tracker.core.application.user.auth.dto.output;

import java.util.UUID;

public record CreateUserOutputDTO(
        UUID id,
        String username,
        String name,
        String email) {
}
