package com.vmarcante.time_tracker.core.domain.user.session.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.user.session.model.UserSession;

public interface UserSessionRepository {

    UserSession save(UserSession userSession);

    Optional<UserSession> findById(UUID id);

    Optional<UserSession> findByRefreshToken(String refreshToken);

    List<UserSession> findActiveByUserId(UUID userId);

    void invalidateAllByUserId(UUID userId);

    void invalidateById(UUID id);

    void deleteExpiredSessions();
}
