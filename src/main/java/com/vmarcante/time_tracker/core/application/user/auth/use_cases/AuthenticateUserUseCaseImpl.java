package com.vmarcante.time_tracker.core.application.user.auth.use_cases;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.UserLoginInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.output.AuthenticationOutputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.in.AuthenticateUserUseCase;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.user.auth.port.JwtPort;
import com.vmarcante.time_tracker.core.domain.user.auth.repository.UserAuthRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.services.ValidateUserAuthCredentialsService;
import com.vmarcante.time_tracker.core.domain.user.session.model.UserSession;
import com.vmarcante.time_tracker.core.domain.user.session.repository.UserSessionRepository;
import com.vmarcante.time_tracker.core.domain.user.session.service.GenerateUserSessionService;
import com.vmarcante.time_tracker.core.domain.user.session.service.RefreshTokenCacheService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthenticateUserUseCaseImpl implements AuthenticateUserUseCase {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int MAX_ACTIVE_SESSIONS = 5;

    private final UserAuthRepository userAuthRepository;
    private final ValidateUserAuthCredentialsService validateUserAuthCredentialsService;
    private final JwtPort jwtPort;
    private final GenerateUserSessionService generateUserSessionService;
    private final UserSessionRepository userSessionRepository;
    private final RefreshTokenCacheService refreshTokenCacheService;

    private final Long accessTokenExpirationMs;
    private final Long refreshTokenExpirationMs;

    public AuthenticateUserUseCaseImpl(
            UserAuthRepository userAuthRepository,
            ValidateUserAuthCredentialsService validateUserAuthCredentialsService,
            JwtPort jwtPort,
            GenerateUserSessionService generateUserSessionService,
            UserSessionRepository userSessionRepository,
            RefreshTokenCacheService refreshTokenCacheService,
            @Value("${jwt.access-token.expiration:300000}") Long accessTokenExpirationMs,
            @Value("${jwt.refresh-token.expiration:604800000}") Long refreshTokenExpirationMs) {
        this.userAuthRepository = userAuthRepository;
        this.validateUserAuthCredentialsService = validateUserAuthCredentialsService;
        this.jwtPort = jwtPort;
        this.generateUserSessionService = generateUserSessionService;
        this.userSessionRepository = userSessionRepository;
        this.refreshTokenCacheService = refreshTokenCacheService;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Override
    public AuthenticationOutputDTO execute(UserLoginInputDTO input) throws ApplicationException {
        return execute(input, null, null, null);
    }

    @Override
    public AuthenticationOutputDTO execute(UserLoginInputDTO input, String deviceInfo, String ipAddress,
            String userAgent) throws ApplicationException {
        log.debug("[User Authentication] Trying to authenticate user {}", input.username());

        Optional<UserAuth> userAuthOpt = userAuthRepository.findByUsernameWithPerson(input.username());

        if (userAuthOpt.isEmpty()) {
            log.warn("[User Authentication] User not found: {}", input.username());
            throw new ApplicationException("user.invalid.credentials", null, HttpStatus.UNAUTHORIZED);
        }

        UserAuth userAuth = userAuthOpt.get();

        if (!userAuth.getActive()) {
            log.warn("[User Authentication] Inactive user attempted login: {}", input.username());
            throw new ApplicationException("user.invalid.credentials", null, HttpStatus.UNAUTHORIZED);
        }

        if (userAuth.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            log.warn("[User Authentication] User account locked due to too many failed attempts: {}", input.username());
            throw new ApplicationException("user.account.locked", null, HttpStatus.FORBIDDEN);
        }

        boolean passwordValid = validateUserAuthCredentialsService.execute(
                userAuth,
                input.password());

        if (!passwordValid) {
            log.warn("[User Authentication] Invalid password for user: {}", input.username());

            userAuth.setFailedAttempts(userAuth.getFailedAttempts() + 1);
            userAuthRepository.save(userAuth);

            throw new ApplicationException("user.invalid.credentials", null, HttpStatus.UNAUTHORIZED);
        }

        if (!userAuth.getUserConfirmed()) {
            log.warn("[User Authentication] User not confirmed attempted login: {}", input.username());
            throw new ApplicationException("user.not.confirmed", null, HttpStatus.FORBIDDEN);
        }

        userAuth.setFailedAttempts(0);
        userAuth.setLastLogin(LocalDateTime.now());
        userAuthRepository.save(userAuth);

        List<UserSession> activeSessions = userSessionRepository.findActiveByUserId(userAuth.getId());
        if (activeSessions.size() >= MAX_ACTIVE_SESSIONS) {

            UserSession oldestSession = activeSessions.stream()
                    .min(Comparator.comparing(UserSession::getCreatedAt))
                    .orElseThrow();

            userSessionRepository.invalidateById(oldestSession.getId());
            refreshTokenCacheService.invalidateToken(oldestSession.getRefreshToken());

            log.info("[User Authentication] Session limit reached, invalidated oldest session | User: {} | Session: {}",
                    input.username(), oldestSession.getId());
        }

        String accessToken = jwtPort.generateAccessToken(userAuth);
        UserSession session = generateUserSessionService.createSession(userAuth, deviceInfo, ipAddress, userAgent);

        log.info("[User Authentication] User authenticated successfully: {}", input.username());

        return new AuthenticationOutputDTO(
                accessToken,
                session.getRefreshToken(),
                accessTokenExpirationMs,
                refreshTokenExpirationMs);
    }

}
