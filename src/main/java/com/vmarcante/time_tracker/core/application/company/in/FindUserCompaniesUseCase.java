package com.vmarcante.time_tracker.core.application.company.in;

import java.util.List;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanySummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface FindUserCompaniesUseCase {

    List<CompanySummaryOutputDTO> execute() throws ApplicationException;
}
