package com.vmarcante.time_tracker.core.application.company.membership.in;

import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.company.membership.dto.input.MembershipFilterInputDTO;
import com.vmarcante.time_tracker.core.application.company.membership.dto.output.CompanyInvitationOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface ListMyInvitationsUseCase {

    PageWrapperDTO<CompanyInvitationOutputDTO> execute(MembershipFilterInputDTO filter)
            throws ApplicationException;
}
