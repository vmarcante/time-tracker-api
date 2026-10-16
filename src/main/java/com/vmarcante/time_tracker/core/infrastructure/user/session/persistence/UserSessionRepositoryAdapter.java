package com.vmarcante.time_tracker.core.infrastructure.user.session.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.vmarcante.time_tracker.core.domain.user.session.model.filter.UserSessionFilterMapper;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.user.session.model.UserSession;
import com.vmarcante.time_tracker.core.domain.user.session.model.filter.UserSessionFilter;
import com.vmarcante.time_tracker.core.domain.user.session.repository.UserSessionRepository;
import com.vmarcante.time_tracker.core.domain.user.session.port.RefreshTokenCachePort;
import com.vmarcante.time_tracker.core.infrastructure.user.session.mapper.UserSessionPersistenceMapper;

@Component
public class UserSessionRepositoryAdapter implements UserSessionRepository {

    private final UserSessionJpaRepository jpaRepository;
    private final RefreshTokenCachePort refreshTokenCacheService;

    public UserSessionRepositoryAdapter(
            UserSessionJpaRepository jpaRepository,
            RefreshTokenCachePort refreshTokenCacheService) {
        this.jpaRepository = jpaRepository;
        this.refreshTokenCacheService = refreshTokenCacheService;
    }

    @Override
    public UserSession save(UserSession userSession) {
        UserSessionJpaEntity entity = UserSessionPersistenceMapper.toEntity(userSession);
        UserSessionJpaEntity savedEntity = jpaRepository.save(entity);
        return UserSessionPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<UserSession> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(UserSessionPersistenceMapper::toDomain);
    }

    @Override
    public Optional<UserSession> findByIdForUpdate(UUID id) {
        return jpaRepository.findByIdForUpdate(id)
                .map(UserSessionPersistenceMapper::toDomain);
    }

    @Override
    public Optional<UserSession> findOne(UserSessionFilter filter) {
        return jpaRepository.findOne(UserSessionRepositorySpecification.withFilter(filter))
                .map(UserSessionPersistenceMapper::toDomain);
    }

    @Override
    public List<UserSession> search(UserSessionFilter filter) {
        return jpaRepository.findAll(UserSessionRepositorySpecification.withFilter(filter)).stream()
                .map(UserSessionPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void invalidateAllByUserId(UUID userId) {
        UserSessionFilter filter = UserSessionFilterMapper.activeByUserId(userId);
        List<UserSessionJpaEntity> activeSessions = jpaRepository
                .findAll(UserSessionRepositorySpecification.withFilter(filter));

        Set<String> refreshTokens = activeSessions.stream()
                .map(UserSessionJpaEntity::getRefreshToken)
                .collect(Collectors.toSet());

        jpaRepository.invalidateAllByUserId(userId);

        refreshTokenCacheService.invalidateAllByUserId(userId, refreshTokens);
    }

    @Override
    public void invalidateById(UUID id) {
        jpaRepository.invalidateById(id);
    }

    @Override
    public void deleteExpiredSessions() {
        jpaRepository.deleteExpiredSessions(LocalDateTime.now());
    }
}
