package com.vmarcante.time_tracker.core.application.company.in;

import com.vmarcante.time_tracker.core.application.company.dto.input.CreateCompanyInputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.CreateCompanyOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface CreateCompanyUseCase {

    CreateCompanyOutputDTO execute(CreateCompanyInputDTO input) throws ApplicationException;
}
