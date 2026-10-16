package com.vmarcante.time_tracker.core.application.company.membership.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.company.membership.dto.input.RejectMembershipInputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface RejectMembershipRequestUseCase {

    void execute(UUID companyId, UUID membershipId, RejectMembershipInputDTO input) throws ApplicationException;
}
