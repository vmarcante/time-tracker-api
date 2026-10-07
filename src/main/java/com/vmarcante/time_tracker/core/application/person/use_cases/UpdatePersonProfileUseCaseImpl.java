package com.vmarcante.time_tracker.core.application.person.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.person.dto.UpdatePersonProfileInputDTO;
import com.vmarcante.time_tracker.core.application.person.in.UpdatePersonProfileUseCase;
import com.vmarcante.time_tracker.core.application.person.policy.UpdatePersonProfileInputValidationPolicy;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;
import com.vmarcante.time_tracker.core.shared.vo.Phone;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UpdatePersonProfileUseCaseImpl implements UpdatePersonProfileUseCase {

    private final PersonRepository personRepository;
    private final SecurityContextPort context;
    private final UpdatePersonProfileInputValidationPolicy validationPolicy;

    public UpdatePersonProfileUseCaseImpl(
            PersonRepository personRepository,
            SecurityContextPort context,
            UpdatePersonProfileInputValidationPolicy validationPolicy) {
        this.personRepository = personRepository;
        this.context = context;
        this.validationPolicy = validationPolicy;
    }

    @Override
    @Transactional
    public void execute(UpdatePersonProfileInputDTO input) throws ApplicationException {
        Optional<UUID> currentUserId = context.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID userId = currentUserId.get();

        validationPolicy.validate(input);

        Person person = personRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException("user.not.found", null));

        person.setName(input.name().trim());

        if (input.phone() != null) {
            person.setPhone(new Phone(input.phone()));
        }

        if (input.age() != null) {
            person.setAge(input.age());
        }

        personRepository.save(person);

        log.info("[Update Profile] User {} profile updated", userId);
    }
}
