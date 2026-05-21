package com.vmarcante.time_tracker.core.domain.user.auth.model;

import java.util.Locale;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.user.enums.UserRoleType;

public record UserContextData(
        UUID userId,
        Integer userSeqId,
        String username,
        String accessToken,
        UserRoleType role,
        Locale locale) {

}
