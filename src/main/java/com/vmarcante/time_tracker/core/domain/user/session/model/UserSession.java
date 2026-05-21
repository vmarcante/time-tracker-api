package com.vmarcante.time_tracker.core.domain.user.session.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.model.BaseAuthDomainModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserSession extends BaseAuthDomainModel<UUID, Integer> {

    private UUID userId;
    private String refreshToken;
    private String refreshTokenHash;
    private LocalDateTime refreshTokenExpiry;
    private String deviceInfo;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime lastActivityAt;
}
