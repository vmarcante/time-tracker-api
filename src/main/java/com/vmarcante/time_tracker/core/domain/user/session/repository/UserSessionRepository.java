package com.vmarcante.time_tracker.core.domain.user.session.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.user.session.model.UserSession;
import com.vmarcante.time_tracker.core.domain.user.session.model.filter.UserSessionFilter;

public interface UserSessionRepository {

    UserSession save(UserSession userSession);

    Optional<UserSession> findById(UUID id);

    Optional<UserSession> findByIdForUpdate(UUID id);

    Optional<UserSession> findOne(UserSessionFilter filter);

    List<UserSession> search(UserSessionFilter filter);

    void invalidateAllByUserId(UUID userId);

    void invalidateById(UUID id);

    void deleteExpiredSessions();
}
