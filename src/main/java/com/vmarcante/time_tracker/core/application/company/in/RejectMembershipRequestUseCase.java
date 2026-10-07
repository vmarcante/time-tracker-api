package com.vmarcante.time_tracker.core.application.company.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.company.dto.input.RejectMembershipInputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface RejectMembershipRequestUseCase {

    void execute(UUID companyId, UUID membershipId, RejectMembershipInputDTO input) throws ApplicationException;
}
