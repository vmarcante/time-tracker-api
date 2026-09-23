package com.vmarcante.time_tracker.core.application.company.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface FindCompanyByIdUseCase {

    CompanyDetailOutputDTO execute(UUID companyId) throws ApplicationException;
}
