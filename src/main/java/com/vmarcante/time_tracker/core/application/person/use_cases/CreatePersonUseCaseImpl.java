package com.vmarcante.time_tracker.core.application.person.use_cases;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.person.dto.CreatePersonDTO;
import com.vmarcante.time_tracker.core.application.person.in.CreatePersonUseCase;
import com.vmarcante.time_tracker.core.application.person.mapper.PersonMapper;
import com.vmarcante.time_tracker.core.application.person.policy.CreatePersonInputValidationPolicy;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;

@Service
public class CreatePersonUseCaseImpl implements CreatePersonUseCase {

    private final PersonRepository personRepository;
    private final CreatePersonInputValidationPolicy validationPolicy;

    public CreatePersonUseCaseImpl(
            PersonRepository personRepository,
            CreatePersonInputValidationPolicy validationPolicy) {
        this.personRepository = personRepository;
        this.validationPolicy = validationPolicy;
    }

    @Override
    @Transactional
    public Person execute(CreatePersonDTO input) throws ApplicationException {
        validationPolicy.validate(input);

        Person person = PersonMapper.toModel(input);
        return personRepository.save(person);
    }
}
