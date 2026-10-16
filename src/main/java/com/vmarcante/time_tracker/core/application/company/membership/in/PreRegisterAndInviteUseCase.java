package com.vmarcante.time_tracker.core.application.company.membership.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.company.membership.dto.input.PreRegisterAndInviteInputDTO;
import com.vmarcante.time_tracker.core.application.company.membership.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface PreRegisterAndInviteUseCase {

    CompanyMemberOutputDTO execute(UUID companyId, PreRegisterAndInviteInputDTO input) throws ApplicationException;
}
