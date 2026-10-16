package com.vmarcante.time_tracker.core.application.company.membership.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface RemoveMemberUseCase {

    void execute(UUID companyId, UUID membershipId) throws ApplicationException;
}
