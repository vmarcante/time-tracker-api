package com.vmarcante.time_tracker.core.application.company.overview.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.company.overview.dto.output.CompanyMenuOverviewOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface GetCompanyMenuOverviewUseCase {

    CompanyMenuOverviewOutputDTO execute(UUID companyId) throws ApplicationException;
}
