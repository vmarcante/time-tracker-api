package com.vmarcante.time_tracker.core.application.user.orchestrator;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.CreateUserAuthDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.CreateUserInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.output.CreateUserOutputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.in.CheckUsernameAvailabilityUseCase;
import com.vmarcante.time_tracker.core.application.user.auth.in.CreateUserUseCase;
import com.vmarcante.time_tracker.core.application.person.dto.CreatePersonDTO;
import com.vmarcante.time_tracker.core.application.person.in.CheckEmailAvailabilityUseCase;
import com.vmarcante.time_tracker.core.application.person.in.CreatePersonUseCase;
import com.vmarcante.time_tracker.core.domain.user.auth.event.UserAuthCreatedEvent;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserRegistrationOrchestrator {

    private final CreateUserUseCase createUserUseCase;
    private final CreatePersonUseCase createPersonUseCase;
    private final CheckEmailAvailabilityUseCase checkEmailAvailabilityUseCase;
    private final CheckUsernameAvailabilityUseCase checkUsernameAvailabilityUseCase;
    private final PersonRepository personRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UserRegistrationOrchestrator(
            CreateUserUseCase createUserUseCase,
            CreatePersonUseCase createPersonUseCase,
            CheckEmailAvailabilityUseCase checkEmailAvailabilityUseCase,
            CheckUsernameAvailabilityUseCase checkUsernameAvailabilityUseCase,
            PersonRepository personRepository,
            ApplicationEventPublisher eventPublisher) {
        this.createUserUseCase = createUserUseCase;
        this.createPersonUseCase = createPersonUseCase;
        this.checkEmailAvailabilityUseCase = checkEmailAvailabilityUseCase;
        this.checkUsernameAvailabilityUseCase = checkUsernameAvailabilityUseCase;
        this.personRepository = personRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CreateUserOutputDTO execute(CreateUserInputDTO input) throws ApplicationException {

        log.debug("[User Registration] Starting user registration process for {}", input.username());

        if (!checkUsernameAvailabilityUseCase.execute(input.username())) {
            log.warn("[User Registration] Attempt to register with unavailable username: {}", input.username());
            throw new ApplicationException("user.username.unavailable", null);
        }

        if (!checkEmailAvailabilityUseCase.execute(input.email().address())) {
            log.warn("[User Registration] Attempt to register with unavailable email: {}", input.email().address());
            throw new ApplicationException("user.email.unavailable", null);
        }

        // Validates and create person
        CreatePersonDTO personInput = new CreatePersonDTO(
                input.name(),
                input.age(),
                input.email(),
                input.phone(),
                input.locale());

        Person person = createPersonUseCase.execute(personInput);

        // Validates and create user authentication data
        CreateUserAuthDTO userAuthInput = new CreateUserAuthDTO(
                person.getId(),
                input.username(),
                input.password());

        UserAuth userAuth = createUserUseCase.execute(userAuthInput);

        // Updates the person with createdBy and updatedBy referencing the created user
        personRepository.updateAuditFields(person.getId(), userAuth.getId(), userAuth.getId());

        UserAuthCreatedEvent emailEvent = new UserAuthCreatedEvent(person, userAuth.getAccessToken());
        eventPublisher.publishEvent(emailEvent);

        // Returns to user
        CreateUserOutputDTO userAuthOutput = new CreateUserOutputDTO(
                person.getId(),
                userAuth.getUsername(),
                person.getName(),
                person.getEmail().address());
        log.info("[User Registration] User registered successfully: {}", userAuthOutput.username());

        return userAuthOutput;
    }

}
