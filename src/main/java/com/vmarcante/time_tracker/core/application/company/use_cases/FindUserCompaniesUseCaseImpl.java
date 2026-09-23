package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanySummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.FindUserCompaniesUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class FindUserCompaniesUseCaseImpl implements FindUserCompaniesUseCase {

    private final CompanyRepository companyRepository;
    private final SecurityContextPort securityContext;

    public FindUserCompaniesUseCaseImpl(
            CompanyRepository companyRepository,
            SecurityContextPort securityContext) {
        this.companyRepository = companyRepository;
        this.securityContext = securityContext;
    }

    @Override
    public Page<CompanySummaryOutputDTO> execute(Pageable pageable) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        return companyRepository.findActiveCompaniesByUserId(currentUserId.get(), pageable)
                .map(CompanySummaryOutputDTO::from);
    }
}
