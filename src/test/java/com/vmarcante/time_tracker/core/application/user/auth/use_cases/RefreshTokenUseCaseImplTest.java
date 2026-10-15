package com.vmarcante.time_tracker.core.application.user.auth.use_cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
import com.vmarcante.time_tracker.core.shared.utils.TokenHashUtils;

class RefreshTokenUseCaseImplTest {

    private JwtPort jwtPort;
    private UserAuthRepository userAuthRepository;
    private UserSessionRepository userSessionRepository;
    private RefreshTokenCacheService refreshTokenCacheService;
    private RefreshTokenUseCase useCase;

    private static final String OLD_TOKEN = "old-refresh-token";
    private static final String NEW_TOKEN = "new-refresh-token";
    private static final String NEW_ACCESS_TOKEN = "new-access-token";
    private static final UUID SESSION_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        jwtPort = mock(JwtPort.class);
        userAuthRepository = mock(UserAuthRepository.class);
        userSessionRepository = mock(UserSessionRepository.class);
        refreshTokenCacheService = mock(RefreshTokenCacheService.class);
        useCase = new RefreshTokenUseCaseImpl(
                jwtPort,
                userAuthRepository,
                userSessionRepository,
                refreshTokenCacheService,
                604800000L,
                300000L);
    }

    @Test
    void givenValidToken_whenExecuting_thenRotatesTokenAndHashAndCache() {
        // Arrange
        UserSession session = activeSession(OLD_TOKEN);
        UserAuth userAuth = activeUser();

        when(jwtPort.validateToken(OLD_TOKEN)).thenReturn(true);
        when(jwtPort.extractTokenType(OLD_TOKEN)).thenReturn("refresh");
        when(jwtPort.generateAccessToken(userAuth)).thenReturn(NEW_ACCESS_TOKEN);
        when(jwtPort.generateRefreshToken(userAuth)).thenReturn(NEW_TOKEN);
        when(refreshTokenCacheService.getSessionId(OLD_TOKEN)).thenReturn(null);
        when(userSessionRepository.findByRefreshToken(OLD_TOKEN)).thenReturn(Optional.of(session));
        when(userSessionRepository.findByIdForUpdate(SESSION_ID)).thenReturn(Optional.of(session));
        when(userAuthRepository.findById(USER_ID)).thenReturn(Optional.of(userAuth));

        // Act
        AuthenticationOutputDTO output = useCase.execute(new RefreshTokenInputDTO(OLD_TOKEN));

        // Assert
        assertThat(output.accessToken()).isEqualTo(NEW_ACCESS_TOKEN);
        assertThat(output.refreshToken()).isEqualTo(NEW_TOKEN);

        ArgumentCaptor<UserSession> captor = ArgumentCaptor.forClass(UserSession.class);
        verify(userSessionRepository).save(captor.capture());
        UserSession saved = captor.getValue();
        assertThat(saved.getRefreshToken()).isEqualTo(NEW_TOKEN);
        assertThat(saved.getRefreshTokenHash())
                .isEqualTo(TokenHashUtils.generateHash(NEW_TOKEN));
        assertThat(saved.getRefreshTokenExpiry()).isAfter(LocalDateTime.now());

        verify(refreshTokenCacheService).invalidateToken(OLD_TOKEN);
        verify(refreshTokenCacheService).cacheToken(
                eq(NEW_TOKEN), eq(SESSION_ID), eq(Duration.ofMillis(604800000L)));
    }

    @Test
    void givenStaleCacheEntry_whenExecuting_thenFallsBackToHashLookup() {
        // Arrange
        UserSession cachedSession = activeSession("rotated-away-token");
        UserSession dbSession = activeSession(OLD_TOKEN);
        UserAuth userAuth = activeUser();

        when(jwtPort.validateToken(OLD_TOKEN)).thenReturn(true);
        when(jwtPort.extractTokenType(OLD_TOKEN)).thenReturn("refresh");
        when(jwtPort.generateAccessToken(userAuth)).thenReturn(NEW_ACCESS_TOKEN);
        when(jwtPort.generateRefreshToken(userAuth)).thenReturn(NEW_TOKEN);
        when(refreshTokenCacheService.getSessionId(OLD_TOKEN)).thenReturn(SESSION_ID);
        when(userSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(cachedSession));
        when(userSessionRepository.findByRefreshToken(OLD_TOKEN)).thenReturn(Optional.of(dbSession));
        when(userSessionRepository.findByIdForUpdate(SESSION_ID)).thenReturn(Optional.of(dbSession));
        when(userAuthRepository.findById(USER_ID)).thenReturn(Optional.of(userAuth));

        // Act
        AuthenticationOutputDTO output = useCase.execute(new RefreshTokenInputDTO(OLD_TOKEN));

        // Assert
        assertThat(output.refreshToken()).isEqualTo(NEW_TOKEN);
        verify(userSessionRepository).findByRefreshToken(OLD_TOKEN);
    }

    @Test
    void givenUnknownToken_whenExecuting_thenThrowsUnauthorized() {
        // Arrange
        when(jwtPort.validateToken(OLD_TOKEN)).thenReturn(true);
        when(jwtPort.extractTokenType(OLD_TOKEN)).thenReturn("refresh");
        when(refreshTokenCacheService.getSessionId(OLD_TOKEN)).thenReturn(null);
        when(userSessionRepository.findByRefreshToken(OLD_TOKEN)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(new RefreshTokenInputDTO(OLD_TOKEN)))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("user.refresh.token.invalid");

        verify(userSessionRepository, never()).save(any());
    }

    @Test
    void givenAccessToken_whenExecuting_thenThrowsUnauthorized() {
        // Arrange
        when(jwtPort.validateToken(OLD_TOKEN)).thenReturn(true);
        when(jwtPort.extractTokenType(OLD_TOKEN)).thenReturn("access");

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(new RefreshTokenInputDTO(OLD_TOKEN)))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("user.refresh.token.invalid");

        verify(userSessionRepository, never()).findByRefreshToken(any());
        verify(userSessionRepository, never()).save(any());
    }

    @Test
    void givenConcurrentRotation_whenExecuting_thenThrowsUnauthorized() {
        // Arrange
        UserSession session = activeSession(OLD_TOKEN);
        UserSession rotatedSession = activeSession("already-rotated-token");

        when(jwtPort.validateToken(OLD_TOKEN)).thenReturn(true);
        when(jwtPort.extractTokenType(OLD_TOKEN)).thenReturn("refresh");
        when(refreshTokenCacheService.getSessionId(OLD_TOKEN)).thenReturn(null);
        when(userSessionRepository.findByRefreshToken(OLD_TOKEN)).thenReturn(Optional.of(session));
        when(userSessionRepository.findByIdForUpdate(SESSION_ID)).thenReturn(Optional.of(rotatedSession));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(new RefreshTokenInputDTO(OLD_TOKEN)))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("user.refresh.token.invalid");

        verify(userSessionRepository, never()).save(any());
        verify(refreshTokenCacheService, never()).cacheToken(any(), any(), any());
    }

    @Test
    void givenExpiredSession_whenExecuting_thenThrowsExpired() {
        // Arrange
        UserSession session = activeSession(OLD_TOKEN);
        session.setRefreshTokenExpiry(LocalDateTime.now().minusMinutes(1));

        when(jwtPort.validateToken(OLD_TOKEN)).thenReturn(true);
        when(jwtPort.extractTokenType(OLD_TOKEN)).thenReturn("refresh");
        when(refreshTokenCacheService.getSessionId(OLD_TOKEN)).thenReturn(null);
        when(userSessionRepository.findByRefreshToken(OLD_TOKEN)).thenReturn(Optional.of(session));
        when(userSessionRepository.findByIdForUpdate(SESSION_ID)).thenReturn(Optional.of(session));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(new RefreshTokenInputDTO(OLD_TOKEN)))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("user.refresh.token.expired");

        verify(userSessionRepository, never()).save(any());
    }

    private UserSession activeSession(String refreshToken) {
        UserSession session = new UserSession();
        session.setId(SESSION_ID);
        session.setUserId(USER_ID);
        session.setRefreshToken(refreshToken);
        session.setRefreshTokenHash(TokenHashUtils.generateHash(refreshToken));
        session.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));
        session.setActive(true);
        return session;
    }

    private UserAuth activeUser() {
        UserAuth userAuth = new UserAuth();
        userAuth.setId(USER_ID);
        userAuth.setUsername("john");
        userAuth.setActive(true);
        userAuth.setUserConfirmed(true);
        return userAuth;
    }
}
