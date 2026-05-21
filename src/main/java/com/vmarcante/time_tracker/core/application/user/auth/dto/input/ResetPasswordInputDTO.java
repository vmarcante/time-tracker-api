package com.vmarcante.time_tracker.core.application.user.auth.dto.input;

public record ResetPasswordInputDTO(
        String token,
        String newPassword) {
}
