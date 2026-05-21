package com.vmarcante.time_tracker.core.infraestructure.user.session.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.domain.user.session.model.UserSession;
import com.vmarcante.time_tracker.core.domain.user.session.repository.UserSessionRepository;
import com.vmarcante.time_tracker.core.domain.user.session.service.RefreshTokenCacheService;
import com.vmarcante.time_tracker.core.infraestructure.user.session.mapper.UserSessionPersistenceMapper;
import com.vmarcante.time_tracker.core.shared.utils.TokenHashUtils;

@Component
public class UserSessionRepositoryAdapter implements UserSessionRepository {

    private final UserSessionJpaRepository jpaRepository;
    private final RefreshTokenCacheService refreshTokenCacheService;

    public UserSessionRepositoryAdapter(
            UserSessionJpaRepository jpaRepository,
            RefreshTokenCacheService refreshTokenCacheService) {
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
    public Optional<UserSession> findByRefreshToken(String refreshToken) {
        String tokenHash = TokenHashUtils.generateHash(refreshToken);
        return jpaRepository.findByRefreshTokenHashAndActiveTrue(tokenHash)
                .map(UserSessionPersistenceMapper::toDomain);
    }

    @Override
    public List<UserSession> findActiveByUserId(UUID userId) {
        return jpaRepository.findByUserIdAndActiveTrue(userId).stream()
                .map(UserSessionPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void invalidateAllByUserId(UUID userId) {
        List<UserSessionJpaEntity> activeSessions = jpaRepository.findByUserIdAndActiveTrue(userId);
        
        Set<String> refreshTokens = activeSessions.stream()
                .map(UserSessionJpaEntity::getRefreshToken)
                .collect(Collectors.toSet());
        
        jpaRepository.invalidateAllByUserId(userId);
        
        refreshTokenCacheService.invalidateAllByUserId(userId, refreshTokens);
    }

    @Override
    @Transactional
    public void invalidateById(UUID id) {
        jpaRepository.invalidateById(id);
    }

    @Override
    @Transactional
    public void deleteExpiredSessions() {
        jpaRepository.deleteExpiredSessions(LocalDateTime.now());
    }
}
