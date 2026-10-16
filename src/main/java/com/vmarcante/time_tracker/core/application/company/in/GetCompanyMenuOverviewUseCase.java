package com.vmarcante.time_tracker.core.application.company.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyMenuOverviewOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface GetCompanyMenuOverviewUseCase {

    CompanyMenuOverviewOutputDTO execute(UUID companyId) throws ApplicationException;
}
