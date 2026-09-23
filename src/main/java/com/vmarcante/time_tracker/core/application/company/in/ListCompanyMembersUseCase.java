package com.vmarcante.time_tracker.core.application.company.in;

import java.util.List;
import java.util.UUID;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface ListCompanyMembersUseCase {

    List<CompanyMemberOutputDTO> execute(UUID companyId) throws ApplicationException;
}
