package com.vmarcante.time_tracker.core.application.company.overview.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.company.overview.dto.output.CompanyOverviewOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface GetCompanyOverviewUseCase {

    CompanyOverviewOutputDTO execute(UUID companyId) throws ApplicationException;
}
