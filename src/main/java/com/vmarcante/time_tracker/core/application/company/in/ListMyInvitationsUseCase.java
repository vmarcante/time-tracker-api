package com.vmarcante.time_tracker.core.application.company.in;

import java.util.List;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyInvitationOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface ListMyInvitationsUseCase {

    List<CompanyInvitationOutputDTO> execute() throws ApplicationException;
}
