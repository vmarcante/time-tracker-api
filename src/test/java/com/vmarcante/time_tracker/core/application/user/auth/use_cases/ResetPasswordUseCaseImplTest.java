package com.vmarcante.time_tracker.core.application.user.auth.use_cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import com.vmarcante.time_tracker.base.domain.event.DomainEventPublisher;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.ResetPasswordInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.in.ResetPasswordUseCase;
import com.vmarcante.time_tracker.core.application.user.auth.policy.CreateUserInputValidationPolicy;
import com.vmarcante.time_tracker.core.domain.user.auth.event.PasswordChangedEvent;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.user.auth.port.PasswordEncryptionPort;
import com.vmarcante.time_tracker.core.domain.user.auth.repository.UserAuthRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.services.GeneratePasswordHashService;
import com.vmarcante.time_tracker.core.domain.user.auth.services.GeneratePasswordSaltService;
import com.vmarcante.time_tracker.core.domain.user.session.repository.UserSessionRepository;

class ResetPasswordUseCaseImplTest {

    private UserAuthRepository userAuthRepository;
    private UserSessionRepository userSessionRepository;
    private GeneratePasswordSaltService generatePasswordSaltService;
    private GeneratePasswordHashService generatePasswordHashService;
    private PasswordEncryptionPort passwordEncryption;
    private CreateUserInputValidationPolicy validationPolicy;
    private DomainEventPublisher eventPublisher;
    private ResetPasswordUseCase useCase;

    private static final String TOKEN = "reset-token";
    private static final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        userAuthRepository = mock(UserAuthRepository.class);
        userSessionRepository = mock(UserSessionRepository.class);
        generatePasswordSaltService = mock(GeneratePasswordSaltService.class);
        generatePasswordHashService = mock(GeneratePasswordHashService.class);
        passwordEncryption = mock(PasswordEncryptionPort.class);
        validationPolicy = mock(CreateUserInputValidationPolicy.class);
        eventPublisher = mock(DomainEventPublisher.class);
        useCase = new ResetPasswordUseCaseImpl(
                userAuthRepository,
                userSessionRepository,
                generatePasswordSaltService,
                generatePasswordHashService,
                passwordEncryption,
                validationPolicy,
                eventPublisher,
                15);
    }

    @Test
    void givenLockedUserWithValidToken_whenResetting_thenClearsLockAndInvalidatesSessions() {
        // Arrange
        UserAuth userAuth = lockedUser();

        when(userAuthRepository.findByAccessToken(TOKEN)).thenReturn(Optional.of(userAuth));
        when(passwordEncryption.matches(any(), any())).thenReturn(false);
        when(generatePasswordSaltService.execute()).thenReturn("new-salt");
        when(generatePasswordHashService.execute("NewPass123!", "new-salt")).thenReturn("new-hash");

        // Act
        useCase.execute(new ResetPasswordInputDTO(TOKEN, "NewPass123!"));

        // Assert
        ArgumentCaptor<UserAuth> captor = ArgumentCaptor.forClass(UserAuth.class);
        verify(userAuthRepository).save(captor.capture());
        UserAuth saved = captor.getValue();
        assertThat(saved.getFailedAttempts()).isZero();
        assertThat(saved.getLockedUntil()).isNull();
        assertThat(saved.getPassword()).isEqualTo("new-hash");
        assertThat(saved.getPasswordSalt()).isEqualTo("new-salt");
        assertThat(saved.getPasswordResetRequested()).isFalse();
        assertThat(saved.getAccessToken()).isNull();

        verify(userSessionRepository).invalidateAllByUserId(USER_ID);
        verify(eventPublisher).publish(isA(PasswordChangedEvent.class));
    }

    @Test
    void givenExpiredToken_whenResetting_thenThrowsAndKeepsLock() {
        // Arrange
        UserAuth userAuth = lockedUser();
        userAuth.setLastPasswordResetRequest(LocalDateTime.now().minusMinutes(30));

        when(userAuthRepository.findByAccessToken(TOKEN)).thenReturn(Optional.of(userAuth));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(new ResetPasswordInputDTO(TOKEN, "NewPass123!")))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("user.reset.token.expired");

        verify(userAuthRepository, never()).save(any());
        verify(userSessionRepository, never()).invalidateAllByUserId(any());
    }

    private UserAuth lockedUser() {
        UserAuth userAuth = new UserAuth();
        userAuth.setId(USER_ID);
        userAuth.setUsername("john");
        userAuth.setActive(true);
        userAuth.setFailedAttempts(5);
        userAuth.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        userAuth.setLastPasswordResetRequest(LocalDateTime.now().minusMinutes(5));
        userAuth.setPassword("old-hash");
        userAuth.setPasswordSalt("old-salt");
        return userAuth;
    }
}
