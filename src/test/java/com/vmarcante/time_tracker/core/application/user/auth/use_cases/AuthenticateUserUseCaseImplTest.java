package com.vmarcante.time_tracker.core.application.user.auth.use_cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.UserLoginInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.output.AuthenticationOutputDTO;
import com.vmarcante.time_tracker.core.application.user.session.orchestrator.CreateUserSessionOrchestrator;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.user.auth.port.JwtPort;
import com.vmarcante.time_tracker.core.domain.user.auth.repository.UserAuthRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.services.ValidateUserAuthCredentialsService;
import com.vmarcante.time_tracker.core.domain.user.session.model.UserSession;
import com.vmarcante.time_tracker.core.domain.user.session.repository.UserSessionRepository;
import com.vmarcante.time_tracker.core.domain.user.session.service.RefreshTokenCacheService;

class AuthenticateUserUseCaseImplTest {

    private UserAuthRepository userAuthRepository;
    private ValidateUserAuthCredentialsService validateCredentialsService;
    private JwtPort jwtPort;
    private CreateUserSessionOrchestrator createUserSessionOrchestrator;
    private UserSessionRepository userSessionRepository;
    private RefreshTokenCacheService refreshTokenCacheService;
    private AuthenticateUserUseCaseImpl useCase;

    private static final String USERNAME = "john";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final long LOCKOUT_DURATION_MS = 900_000L;

    @BeforeEach
    void setUp() {
        userAuthRepository = mock(UserAuthRepository.class);
        validateCredentialsService = mock(ValidateUserAuthCredentialsService.class);
        jwtPort = mock(JwtPort.class);
        createUserSessionOrchestrator = mock(CreateUserSessionOrchestrator.class);
        userSessionRepository = mock(UserSessionRepository.class);
        refreshTokenCacheService = mock(RefreshTokenCacheService.class);
        useCase = new AuthenticateUserUseCaseImpl(
                userAuthRepository,
                validateCredentialsService,
                jwtPort,
                createUserSessionOrchestrator,
                userSessionRepository,
                refreshTokenCacheService,
                300_000L,
                604_800_000L,
                LOCKOUT_DURATION_MS);
    }

    @Test
    void givenValidCredentials_whenExecuting_thenResetsLockStateAndReturnsTokens() {
        // Arrange
        UserAuth userAuth = activeUser();
        userAuth.setFailedAttempts(3);
        userAuth.setLockedUntil(LocalDateTime.now().minusMinutes(1));
        UserSession session = sessionWithRefreshToken();

        when(userAuthRepository.findByUsernameWithPerson(USERNAME)).thenReturn(Optional.of(userAuth));
        when(validateCredentialsService.execute(userAuth, "secret")).thenReturn(true);
        when(userSessionRepository.findActiveByUserId(USER_ID)).thenReturn(List.of());
        when(jwtPort.generateAccessToken(userAuth)).thenReturn("access-token");
        when(createUserSessionOrchestrator.createSession(eq(userAuth), any(), any(), any()))
                .thenReturn(session);

        // Act
        AuthenticationOutputDTO output = useCase.execute(new UserLoginInputDTO(USERNAME, "secret"));

        // Assert
        assertThat(output.accessToken()).isEqualTo("access-token");
        assertThat(output.refreshToken()).isEqualTo("refresh-token");

        ArgumentCaptor<UserAuth> captor = ArgumentCaptor.forClass(UserAuth.class);
        verify(userAuthRepository).save(captor.capture());
        UserAuth saved = captor.getValue();
        assertThat(saved.getFailedAttempts()).isZero();
        assertThat(saved.getLockedUntil()).isNull();
        assertThat(saved.getLastLogin()).isNotNull();
    }

    @Test
    void givenInvalidPassword_whenExecuting_thenIncrementsFailedAttempts() {
        // Arrange
        UserAuth userAuth = activeUser();
        userAuth.setFailedAttempts(0);

        when(userAuthRepository.findByUsernameWithPerson(USERNAME)).thenReturn(Optional.of(userAuth));
        when(validateCredentialsService.execute(userAuth, "wrong")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(new UserLoginInputDTO(USERNAME, "wrong")))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("user.invalid.credentials");

        ArgumentCaptor<UserAuth> captor = ArgumentCaptor.forClass(UserAuth.class);
        verify(userAuthRepository).save(captor.capture());
        assertThat(captor.getValue().getFailedAttempts()).isEqualTo(1);
        assertThat(captor.getValue().getLockedUntil()).isNull();
    }

    @Test
    void givenFifthFailedAttempt_whenExecuting_thenLocksAccountTemporarily() {
        // Arrange
        UserAuth userAuth = activeUser();
        userAuth.setFailedAttempts(4);
        LocalDateTime before = LocalDateTime.now();

        when(userAuthRepository.findByUsernameWithPerson(USERNAME)).thenReturn(Optional.of(userAuth));
        when(validateCredentialsService.execute(userAuth, "wrong")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(new UserLoginInputDTO(USERNAME, "wrong")))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("user.invalid.credentials");

        ArgumentCaptor<UserAuth> captor = ArgumentCaptor.forClass(UserAuth.class);
        verify(userAuthRepository).save(captor.capture());
        UserAuth saved = captor.getValue();
        assertThat(saved.getFailedAttempts()).isEqualTo(5);
        assertThat(saved.getLockedUntil())
                .isAfter(before.plusSeconds(LOCKOUT_DURATION_MS / 1000 - 60))
                .isBefore(LocalDateTime.now().plusSeconds(LOCKOUT_DURATION_MS / 1000 + 60));
    }

    @Test
    void givenLockedAccount_whenExecuting_thenThrowsLockedWithoutCheckingPassword() {
        // Arrange
        UserAuth userAuth = activeUser();
        userAuth.setFailedAttempts(5);
        userAuth.setLockedUntil(LocalDateTime.now().plusMinutes(10));

        when(userAuthRepository.findByUsernameWithPerson(USERNAME)).thenReturn(Optional.of(userAuth));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(new UserLoginInputDTO(USERNAME, "secret")))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("user.account.locked");

        verify(validateCredentialsService, never()).execute(any(), any());
        verify(userAuthRepository, never()).save(any());
    }

    @Test
    void givenExpiredLockout_whenExecuting_thenCounterResetsBeforeValidating() {
        // Arrange
        UserAuth userAuth = activeUser();
        userAuth.setFailedAttempts(5);
        userAuth.setLockedUntil(LocalDateTime.now().minusMinutes(1));
        UserSession session = sessionWithRefreshToken();

        when(userAuthRepository.findByUsernameWithPerson(USERNAME)).thenReturn(Optional.of(userAuth));
        when(validateCredentialsService.execute(userAuth, "secret")).thenReturn(true);
        when(userSessionRepository.findActiveByUserId(USER_ID)).thenReturn(List.of());
        when(jwtPort.generateAccessToken(userAuth)).thenReturn("access-token");
        when(createUserSessionOrchestrator.createSession(eq(userAuth), any(), any(), any()))
                .thenReturn(session);

        // Act
        AuthenticationOutputDTO output = useCase.execute(new UserLoginInputDTO(USERNAME, "secret"));

        // Assert
        assertThat(output.accessToken()).isEqualTo("access-token");
        verify(validateCredentialsService).execute(userAuth, "secret");
    }

    private UserAuth activeUser() {
        UserAuth userAuth = new UserAuth();
        userAuth.setId(USER_ID);
        userAuth.setUsername(USERNAME);
        userAuth.setActive(true);
        userAuth.setUserConfirmed(true);
        userAuth.setFailedAttempts(0);
        return userAuth;
    }

    private UserSession sessionWithRefreshToken() {
        UserSession session = new UserSession();
        session.setId(UUID.randomUUID());
        session.setUserId(USER_ID);
        session.setRefreshToken("refresh-token");
        session.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));
        session.setActive(true);
        return session;
    }
}
