package com.vmarcante.time_tracker.core.domain.user.session.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.user.auth.port.JwtPort;
import com.vmarcante.time_tracker.core.domain.user.session.model.UserSession;
import com.vmarcante.time_tracker.core.domain.user.session.repository.UserSessionRepository;
import com.vmarcante.time_tracker.core.shared.utils.TokenHashUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GenerateUserSessionService {

    private final JwtPort jwtPort;
    private final UserSessionRepository userSessionRepository;
    private final RefreshTokenCacheService refreshTokenCacheService;

    @Value("${jwt.refresh-token.expiration:604800000}")
    private Long refreshTokenExpirationMs;

    public GenerateUserSessionService(
            JwtPort jwtPort,
            UserSessionRepository userSessionRepository,
            RefreshTokenCacheService refreshTokenCacheService) {
        this.jwtPort = jwtPort;
        this.userSessionRepository = userSessionRepository;
        this.refreshTokenCacheService = refreshTokenCacheService;
    }

    public UserSession createSession(UserAuth userAuth, String deviceInfo, String ipAddress, String userAgent) {
        log.debug("[GenerateUserSession] Creating session for user: {}", userAuth.getUsername());

        String refreshToken = jwtPort.generateRefreshToken(userAuth);
        String refreshTokenHash = TokenHashUtils.generateHash(refreshToken);
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000);

        UserSession session = new UserSession();
        session.setUserId(userAuth.getId());
        session.setRefreshToken(refreshToken);
        session.setRefreshTokenHash(refreshTokenHash);
        session.setRefreshTokenExpiry(expiryDate);
        session.setDeviceInfo(deviceInfo);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setActive(true);
        session.setLastActivityAt(LocalDateTime.now());
        session.setCreatedBy(userAuth.getId());
        session.setUpdatedBy(userAuth.getId());

        UserSession savedSession = userSessionRepository.save(session);

        refreshTokenCacheService.cacheToken(
                refreshToken,
                savedSession.getId(),
                Duration.ofMillis(refreshTokenExpirationMs)
        );

        log.debug("[GenerateUserSession] Session created successfully: sessionId={}", savedSession.getId());
        return savedSession;
    }
}
