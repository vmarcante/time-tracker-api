package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.FindCompanyByIdUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FindCompanyByIdUseCaseImpl implements FindCompanyByIdUseCase {

    private final CompanyMembershipRepository membershipRepository;
    private final CompanyRepository companyRepository;
    private final SecurityContextPort securityContext;

    public FindCompanyByIdUseCaseImpl(
            CompanyMembershipRepository membershipRepository,
            CompanyRepository companyRepository,
            SecurityContextPort securityContext) {
        this.membershipRepository = membershipRepository;
        this.companyRepository = companyRepository;
        this.securityContext = securityContext;
    }

    @Override
    public CompanyDetailOutputDTO execute(UUID companyId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID userId = currentUserId.get();

        if (!membershipRepository.isMember(userId, companyId)) {
            throw new ApplicationException("company.access.denied", null);
        }

        return companyRepository.findById(companyId)
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .map(CompanyDetailOutputDTO::from)
                .orElseThrow(() -> new ApplicationException("company.not.found", null));
    }
}
