package com.vmarcante.time_tracker.core.application.company.membership.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.company.membership.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface RequestToJoinCompanyUseCase {

    CompanyMemberOutputDTO execute(UUID companyId, String requestReason) throws ApplicationException;
}
