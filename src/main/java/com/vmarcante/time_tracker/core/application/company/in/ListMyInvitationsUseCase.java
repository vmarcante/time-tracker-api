package com.vmarcante.time_tracker.core.application.company.in;

import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.company.dto.input.MembershipFilterInputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyInvitationOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface ListMyInvitationsUseCase {

    PageWrapperDTO<CompanyInvitationOutputDTO> execute(MembershipFilterInputDTO filter)
            throws ApplicationException;
}
