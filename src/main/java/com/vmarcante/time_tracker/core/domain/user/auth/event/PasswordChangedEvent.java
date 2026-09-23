package com.vmarcante.time_tracker.core.domain.user.auth.event;

import java.util.UUID;

import lombok.Data;

@Data
public class PasswordChangedEvent {

    private final UUID userId;
}
