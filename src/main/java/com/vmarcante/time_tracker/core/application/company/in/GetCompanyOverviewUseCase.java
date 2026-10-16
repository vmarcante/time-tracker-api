package com.vmarcante.time_tracker.core.application.company.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyOverviewOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface GetCompanyOverviewUseCase {

    CompanyOverviewOutputDTO execute(UUID companyId) throws ApplicationException;
}
