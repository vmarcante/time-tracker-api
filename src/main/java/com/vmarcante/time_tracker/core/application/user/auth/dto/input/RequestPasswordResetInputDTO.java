package com.vmarcante.time_tracker.core.application.user.auth.dto.input;

public record RequestPasswordResetInputDTO(
        String username,
        String email,
        String locale
) {
    public RequestPasswordResetInputDTO(String username, String email) {
        this(username, email, "pt");
    }
}
