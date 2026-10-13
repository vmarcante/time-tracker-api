package com.vmarcante.time_tracker.core.application.company.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.company.dto.output.PersonInviteLookupOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface LookupPersonForInviteUseCase {

    PersonInviteLookupOutputDTO execute(UUID companyId, String email) throws ApplicationException;
}
