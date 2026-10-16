package com.vmarcante.time_tracker.core.domain.user.session.model.filter;

import java.util.UUID;

public final class UserSessionFilterMapper {

    private UserSessionFilterMapper() {
    }

    public static UserSessionFilter activeByUserId(UUID userId) {
        UserSessionFilter filter = new UserSessionFilter();
        filter.setUserId(userId);
        filter.setActive(true);
        return filter;
    }

    public static UserSessionFilter activeByRefreshToken(String refreshToken) {
        UserSessionFilter filter = new UserSessionFilter();
        filter.setRefreshToken(refreshToken);
        filter.setActive(true);
        return filter;
    }

    public static UserSessionFilter byRefreshToken(String refreshToken) {
        UserSessionFilter filter = new UserSessionFilter();
        filter.setRefreshToken(refreshToken);
        return filter;
    }
}
