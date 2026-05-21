package com.vmarcante.time_tracker.core.application.user.auth.use_cases;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.RefreshTokenInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.output.AuthenticationOutputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.in.RefreshTokenUseCase;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.user.auth.port.JwtPort;
import com.vmarcante.time_tracker.core.domain.user.auth.repository.UserAuthRepository;
import com.vmarcante.time_tracker.core.domain.user.session.model.UserSession;
import com.vmarcante.time_tracker.core.domain.user.session.repository.UserSessionRepository;
import com.vmarcante.time_tracker.core.domain.user.session.service.RefreshTokenCacheService;
import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private final JwtPort jwtPort;
    private final UserAuthRepository userAuthRepository;
    private final UserSessionRepository userSessionRepository;
    private final RefreshTokenCacheService refreshTokenCacheService;
    private final Long refreshTokenExpirationMs;
    private final Long accessTokenExpirationMs;

    public RefreshTokenUseCaseImpl(
            JwtPort jwtPort,
            UserAuthRepository userAuthRepository,
            UserSessionRepository userSessionRepository,
            RefreshTokenCacheService refreshTokenCacheService,
            @Value("${jwt.refresh-token.expiration:604800000}") Long refreshTokenExpirationMs,
            @Value("${jwt.access-token.expiration:300000}") Long accessTokenExpirationMs) {
        this.jwtPort = jwtPort;
        this.userAuthRepository = userAuthRepository;
        this.userSessionRepository = userSessionRepository;
        this.refreshTokenCacheService = refreshTokenCacheService;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    @Override
    public AuthenticationOutputDTO execute(RefreshTokenInputDTO input) throws ApplicationException {
        if (!StringValidationUtils.containsContent(input.refreshToken())) {
            log.warn("[Refresh Token] Refresh token is required");
            throw new ApplicationException("user.refresh.token.required", null, HttpStatus.BAD_REQUEST);
        }

        String token = input.refreshToken().startsWith("Bearer ") ? input.refreshToken().substring(7).trim()
                : input.refreshToken().trim();

        if (!jwtPort.validateToken(token)) {
            log.warn("[Refresh Token] Invalid or expired refresh token");
            throw new ApplicationException("user.refresh.token.invalid", null, HttpStatus.UNAUTHORIZED);
        }

        String tokenType = jwtPort.extractTokenType(token);
        log.debug("[Refresh Token] Token type: {}", tokenType);

        if (!"refresh".equals(tokenType)) {
            log.warn("[Refresh Token] Invalid token type: {}", tokenType);
            throw new ApplicationException("user.refresh.token.invalid", null, HttpStatus.UNAUTHORIZED);
        }

        // Tenta busca no Redis
        UUID sessionId = refreshTokenCacheService.getSessionId(token);
        UserSession session = null;

        if (sessionId != null) {
            log.debug("[Refresh Token] Cache HIT - validating session from DB");
            Optional<UserSession> sessionOpt = userSessionRepository.findById(sessionId);
            if (sessionOpt.isPresent()) {
                session = sessionOpt.get();
            }
        }

        // Fallback para DB (SEGURANÇA)
        if (session == null) {
            log.debug("[Refresh Token] Cache MISS - searching in DB");
            Optional<UserSession> sessionOpt = userSessionRepository.findByRefreshToken(token);
            if (sessionOpt.isEmpty()) {
                log.warn("[Refresh Token] Session not found for refresh token");
                throw new ApplicationException("user.refresh.token.invalid", null, HttpStatus.UNAUTHORIZED);
            }
            session = sessionOpt.get();
        }

        if (!session.getActive()) {
            log.warn("[Refresh Token] Inactive session attempted refresh | Session ID: {}", session.getId());
            throw new ApplicationException("user.session.inactive", null, HttpStatus.UNAUTHORIZED);
        }

        if (session.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            log.warn("[Refresh Token] Expired session attempted refresh | Session ID: {}", session.getId());
            throw new ApplicationException("user.refresh.token.expired", null, HttpStatus.UNAUTHORIZED);
        }

        UUID userId = session.getUserId();
        Optional<UserAuth> userAuthOpt = userAuthRepository.findById(userId);
        if (userAuthOpt.isEmpty()) {
            log.warn("[Refresh Token] User not found for session | User ID: {}", userId);
            throw new ApplicationException("user.invalid.credentials", null, HttpStatus.UNAUTHORIZED);
        }

        UserAuth userAuth = userAuthOpt.get();

        if (!userAuth.getActive()) {
            log.warn("[Refresh Token] Inactive user attempted token refresh | User ID: {}", userId);
            throw new ApplicationException("user.invalid.credentials", null, HttpStatus.UNAUTHORIZED);
        }

        if (!userAuth.getUserConfirmed()) {
            log.warn("[Refresh Token] User not confirmed attempted token refresh | User ID: {}", userId);
            throw new ApplicationException("user.not.confirmed", null, HttpStatus.FORBIDDEN);
        }

        // Gera novos tokens
        String newAccessToken = jwtPort.generateAccessToken(userAuth);
        String newRefreshToken = jwtPort.generateRefreshToken(userAuth);

        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000);
        session.setLastActivityAt(LocalDateTime.now());
        session.setRefreshToken(newRefreshToken);
        session.setRefreshTokenExpiry(expiryDate);
        userSessionRepository.save(session);

        log.debug("[Refresh Token] Token refreshed successfully | User ID: {} | Username: {} | Session ID: {}",
                userAuth.getId(), userAuth.getUsername(), session.getId());

        AuthenticationOutputDTO output = new AuthenticationOutputDTO(newAccessToken, newRefreshToken,
                accessTokenExpirationMs, refreshTokenExpirationMs);
        return output;
    }
}
