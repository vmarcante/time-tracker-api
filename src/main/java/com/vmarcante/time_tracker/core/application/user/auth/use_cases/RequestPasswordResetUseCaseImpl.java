package com.vmarcante.time_tracker.core.application.user.auth.use_cases;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.RequestPasswordResetInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.in.RequestPasswordResetUseCase;
import com.vmarcante.time_tracker.core.domain.user.auth.event.PasswordResetRequestedEvent;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.user.auth.repository.UserAuthRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.services.GenerateRandomAccessTokenService;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;
import com.vmarcante.time_tracker.core.shared.vo.EmailValidator;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RequestPasswordResetUseCaseImpl implements RequestPasswordResetUseCase {

    private final UserAuthRepository userAuthRepository;
    private final PersonRepository personRepository;
    private final GenerateRandomAccessTokenService generateRandomAccessTokenService;
    private final ApplicationEventPublisher eventPublisher;

    public RequestPasswordResetUseCaseImpl(
            UserAuthRepository userAuthRepository,
            PersonRepository personRepository,
            GenerateRandomAccessTokenService generateRandomAccessTokenService,
            ApplicationEventPublisher eventPublisher) {
        this.userAuthRepository = userAuthRepository;
        this.personRepository = personRepository;
        this.generateRandomAccessTokenService = generateRandomAccessTokenService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void execute(RequestPasswordResetInputDTO input) {
        log.debug("[Password Reset] Request received for username: {}", input.username());

        if (!StringValidationUtils.containsContent(input.username())) {
            log.debug("[Password Reset] Invalid username: {}", input.username());
            throw new ApplicationException("user.username.required", null, HttpStatus.BAD_REQUEST);
        }

        if (!EmailValidator.isValid(input.email())) {
            log.debug("[Password Reset] Invalid email: {}", input.email());
            throw new ApplicationException("user.email.invalid", null, HttpStatus.BAD_REQUEST);
        }

        try {
            Optional<UserAuth> userAuthOpt = userAuthRepository.findByUsername(input.username());
            if (userAuthOpt.isEmpty()) {
                log.debug("[Password Reset] User not found: {}", input.username());
                return; // Silencioso
            }

            UserAuth userAuth = userAuthOpt.get();

            if (!userAuth.getActive()) {
                log.debug("[Password Reset] User not active: {}", input.username());
                return; // Silencioso
            }

            Optional<Person> personOpt = personRepository.findById(userAuth.getId());
            if (personOpt.isEmpty()) {
                log.debug("[Password Reset] Person not found for: {}", input.username());
                return; // Silencioso
            }

            Person person = personOpt.get();
            if (!input.email().equalsIgnoreCase(person.getEmail().address())) {
                log.debug("[Password Reset] Email mismatch for user: {}", input.username());
                return; // Silencioso
            }

            String resetToken = generateRandomAccessTokenService.execute();

            userAuth.setAccessToken(resetToken);
            userAuth.setPasswordResetRequested(true);
            userAuth.setLastPasswordResetRequest(LocalDateTime.now());
            userAuthRepository.save(userAuth);

            String userLocale = person.getLocale() != null ? person.getLocale() : input.locale();
            
            PasswordResetRequestedEvent event = new PasswordResetRequestedEvent(
                    userAuth.getId(),
                    userAuth.getUsername(),
                    person.getName(),
                    person.getEmail().address(),
                    resetToken,
                    userLocale);

            eventPublisher.publishEvent(event);

            log.info("[Password Reset] Event published successfully | User: {}", input.username());

        } catch (Exception e) {
            log.error("[Password Reset] Error processing request for username: {}", input.username(), e);
        }
    }
}
