package com.vmarcante.time_tracker.core.application.company.in;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyPreviewOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface FindCompanyByDocumentUseCase {

    CompanyPreviewOutputDTO execute(String document) throws ApplicationException;
}
