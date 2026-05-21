package com.vmarcante.time_tracker.core.application.person.use_cases;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.person.in.CheckEmailAvailabilityUseCase;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;
import com.vmarcante.time_tracker.core.shared.vo.EmailValidator;

@Service
public class CheckEmailAvailabilityUseCaseImpl implements CheckEmailAvailabilityUseCase {

    private final PersonRepository personRepository;

    public CheckEmailAvailabilityUseCaseImpl(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public boolean execute(String email) {
        if (!StringValidationUtils.containsContent(email)) {
            return false;
        }

        String trimmedEmail = email.trim().toLowerCase();

        if (!EmailValidator.isValid(trimmedEmail)) {
            return false;
        }

        return !personRepository.existsByEmail(trimmedEmail);
    }
}
