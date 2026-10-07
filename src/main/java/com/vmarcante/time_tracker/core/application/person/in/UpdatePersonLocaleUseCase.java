package com.vmarcante.time_tracker.core.application.person.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.person.dto.UpdatePersonLocaleInputDTO;

public interface UpdatePersonLocaleUseCase {

    void execute(UpdatePersonLocaleInputDTO input) throws ApplicationException;
}
