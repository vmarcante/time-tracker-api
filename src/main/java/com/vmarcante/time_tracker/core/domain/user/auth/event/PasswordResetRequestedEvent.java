package com.vmarcante.time_tracker.core.domain.user.auth.event;

import java.util.UUID;

import lombok.Data;

@Data
public class PasswordResetRequestedEvent {

    private final UUID userId;
    private final String username;
    private final String name;
    private final String email;
    private final String resetToken;
    private final String locale;

    public PasswordResetRequestedEvent(UUID userId, String username, String name, String email, String resetToken, String locale) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.email = email;
        this.resetToken = resetToken;
        this.locale = locale != null ? locale : "pt";
    }
}
