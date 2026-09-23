package com.vmarcante.time_tracker.core.application.company.in;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

public interface ListCompanyMembersUseCase {

    Page<CompanyMemberOutputDTO> execute(UUID companyId, Pageable pageable) throws ApplicationException;
}
