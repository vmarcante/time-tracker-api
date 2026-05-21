package com.vmarcante.time_tracker.core.domain.user.auth.port;

import java.util.Optional;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.user.auth.model.UserContextData;

public interface SecurityContextPort {

    void setCurrentUser(UserContextData userContext);

    Optional<UserContextData> getCurrentUser();

    Optional<UUID> getCurrentUserId();

    Optional<String> getCurrentUsername();

    void clear();
}
