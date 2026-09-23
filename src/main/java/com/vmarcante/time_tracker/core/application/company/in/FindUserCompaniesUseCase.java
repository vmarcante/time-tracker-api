package com.vmarcante.time_tracker.core.application.company.in;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanySummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface FindUserCompaniesUseCase {

    Page<CompanySummaryOutputDTO> execute(Pageable pageable) throws ApplicationException;
}
