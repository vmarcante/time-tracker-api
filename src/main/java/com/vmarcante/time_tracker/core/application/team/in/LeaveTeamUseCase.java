package com.vmarcante.time_tracker.core.application.team.in;

import java.util.UUID;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface LeaveTeamUseCase {

    void execute(UUID companyId, UUID teamId) throws ApplicationException;
}
