package com.vmarcante.time_tracker.core.application.person.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.person.dto.UpdatePersonProfileInputDTO;

public interface UpdatePersonProfileUseCase {

    void execute(UpdatePersonProfileInputDTO input) throws ApplicationException;
}
