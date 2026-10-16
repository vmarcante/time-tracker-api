package com.vmarcante.time_tracker.core.domain.user.auth.model.filter;

public final class UserAuthFilterMapper {

    private UserAuthFilterMapper() {
    }

    public static UserAuthFilter byUsername(String username) {
        UserAuthFilter filter = new UserAuthFilter();
        filter.setUsername(username);
        return filter;
    }
}
