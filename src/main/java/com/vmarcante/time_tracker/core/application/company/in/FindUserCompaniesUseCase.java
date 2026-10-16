package com.vmarcante.time_tracker.core.application.company.in;

import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.company.dto.input.CompanyFilterInputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.CompanySummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface FindUserCompaniesUseCase {

    PageWrapperDTO<CompanySummaryOutputDTO> execute(CompanyFilterInputDTO filter) throws ApplicationException;
}
