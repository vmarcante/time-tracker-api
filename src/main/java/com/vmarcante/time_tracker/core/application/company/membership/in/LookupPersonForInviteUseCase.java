package com.vmarcante.time_tracker.core.application.company.membership.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.company.membership.dto.output.PersonInviteLookupOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface LookupPersonForInviteUseCase {

    PersonInviteLookupOutputDTO execute(UUID companyId, String email) throws ApplicationException;
}
