package com.vmarcante.time_tracker.core.application.user.auth.use_cases;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

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
import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ResetPasswordUseCaseImpl implements ResetPasswordUseCase {

    private final UserAuthRepository userAuthRepository;
    private final UserSessionRepository userSessionRepository;
    private final GeneratePasswordSaltService generatePasswordSaltService;
    private final GeneratePasswordHashService generatePasswordHashService;
    private final PasswordEncryptionPort passwordEncryption;
    private final CreateUserInputValidationPolicy validationPolicy;
    private final ApplicationEventPublisher eventPublisher;
    private final int resetTokenExpirationMinutes;

    public ResetPasswordUseCaseImpl(
            UserAuthRepository userAuthRepository,
            UserSessionRepository userSessionRepository,
            GeneratePasswordSaltService generatePasswordSaltService,
            GeneratePasswordHashService generatePasswordHashService,
            PasswordEncryptionPort passwordEncryption,
            CreateUserInputValidationPolicy validationPolicy,
            ApplicationEventPublisher eventPublisher,
            @Value("${app.reset-token.expiration-minutes:15}") int resetTokenExpirationMinutes) {
        this.userAuthRepository = userAuthRepository;
        this.userSessionRepository = userSessionRepository;
        this.generatePasswordSaltService = generatePasswordSaltService;
        this.generatePasswordHashService = generatePasswordHashService;
        this.passwordEncryption = passwordEncryption;
        this.validationPolicy = validationPolicy;
        this.eventPublisher = eventPublisher;
        this.resetTokenExpirationMinutes = resetTokenExpirationMinutes;
    }

    @Override
    public void execute(ResetPasswordInputDTO input) throws ApplicationException {
        log.debug("[Reset Password] Processing password reset request");

        if (!StringValidationUtils.containsContent(input.token())) {
            throw new ApplicationException("user.reset.token.required", null);
        }

        if (!StringValidationUtils.containsContent(input.newPassword())) {
            throw new ApplicationException("user.password.required", null);
        }

        Optional<UserAuth> userAuthOpt = userAuthRepository.findByAccessToken(input.token());
        if (userAuthOpt.isEmpty()) {
            log.warn("[Reset Password] Invalid or expired token");
            throw new ApplicationException("user.reset.token.invalid", null);
        }

        UserAuth userAuth = userAuthOpt.get();

        if (!userAuth.getActive()) {
            log.warn("[Reset Password] Inactive user attempted password reset: {}", userAuth.getUsername());
            throw new ApplicationException("user.reset.token.invalid", null);
        }

        if (userAuth.getLastPasswordResetRequest() == null) {
            log.warn("[Reset Password] No reset request timestamp found for user: {}", userAuth.getUsername());
            throw new ApplicationException("user.reset.token.invalid", null);
        }

        Duration timeSinceRequest = Duration.between(userAuth.getLastPasswordResetRequest(), LocalDateTime.now());
        if (timeSinceRequest.toMinutes() > resetTokenExpirationMinutes) {
            log.warn("[Reset Password] Token expired for user: {} | Requested: {} | Expiration: {} minutes",
                    userAuth.getUsername(), userAuth.getLastPasswordResetRequest(), resetTokenExpirationMinutes);
            throw new ApplicationException("user.reset.token.expired", null);
        }

        validationPolicy.validatePassword(input.newPassword());

        String passwordWithSalt = input.newPassword() + userAuth.getPasswordSalt();
        if (passwordEncryption.matches(passwordWithSalt, userAuth.getPassword())) {
            log.warn("[Reset Password] User attempted to use the same password: {}", userAuth.getUsername());
            throw new ApplicationException("user.password.same.as.current", null);
        }

        String newSalt = generatePasswordSaltService.execute();
        String newPasswordHash = generatePasswordHashService.execute(input.newPassword(), newSalt);

        userAuth.setPassword(newPasswordHash);
        userAuth.setPasswordSalt(newSalt);
        userAuth.setAccessToken(null);
        userAuth.setFailedAttempts(0);
        userAuth.setPasswordResetRequested(false);
        userAuth.setLastPasswordChange(LocalDateTime.now());

        userAuthRepository.save(userAuth);

        userSessionRepository.invalidateAllByUserId(userAuth.getId());
        log.info("[Reset Password] All sessions invalidated for user: {}", userAuth.getUsername());

        eventPublisher.publishEvent(new PasswordChangedEvent(userAuth.getId()));

        log.info("[Reset Password] Password reset successfully for user: {} | New salt and hash generated | All sessions invalidated",
                userAuth.getUsername());
    }
}
