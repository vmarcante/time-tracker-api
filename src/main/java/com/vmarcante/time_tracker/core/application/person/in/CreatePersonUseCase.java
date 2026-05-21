package com.vmarcante.time_tracker.core.application.person.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.person.dto.CreatePersonDTO;
import com.vmarcante.time_tracker.core.domain.person.model.Person;

public interface CreatePersonUseCase {

    Person execute(CreatePersonDTO input) throws ApplicationException;
}
