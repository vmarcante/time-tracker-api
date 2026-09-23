package com.vmarcante.time_tracker.core.application.company.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface LeaveCompanyUseCase {

    void execute(UUID companyId) throws ApplicationException;
}
