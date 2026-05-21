package com.vmarcante.time_tracker.core.infraestructure.security.context;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.user.auth.model.UserContextData;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Component
public class SecurityContextAdapter implements SecurityContextPort {

    private static final ThreadLocal<UserContextData> CONTEXT = new ThreadLocal<>();

    @Override
    public void setCurrentUser(UserContextData userContext) {
        CONTEXT.set(userContext);
    }

    @Override
    public Optional<UserContextData> getCurrentUser() {
        return Optional.ofNullable(CONTEXT.get());
    }

    @Override
    public Optional<UUID> getCurrentUserId() {
        return getCurrentUser().map(UserContextData::userId);
    }

    @Override
    public Optional<String> getCurrentUsername() {
        return getCurrentUser().map(UserContextData::username);
    }

    @Override
    public void clear() {
        CONTEXT.remove();
    }
}
