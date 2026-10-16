package com.vmarcante.time_tracker.core.application.company.in;

import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.company.dto.input.MembershipFilterInputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface ListResolvedMembershipsUseCase {

    PageWrapperDTO<CompanyMemberOutputDTO> execute(UUID companyId, MembershipFilterInputDTO filter)
            throws ApplicationException;
}
