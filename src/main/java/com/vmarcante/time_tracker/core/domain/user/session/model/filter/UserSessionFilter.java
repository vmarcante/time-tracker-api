package com.vmarcante.time_tracker.core.domain.user.session.model.filter;

import java.util.UUID;

import lombok.Data;

@Data
public class UserSessionFilter {
    private UUID id;
    private UUID userId;
    private String refreshToken;
    private String refreshTokenHash;
    private Boolean active;
}
