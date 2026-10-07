package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.company.in.CountPendingMembershipsUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class CountPendingMembershipsUseCaseImpl implements CountPendingMembershipsUseCase {

    private final CompanyMembershipRepository membershipRepository;
    private final SecurityContextPort securityContext;

    public CountPendingMembershipsUseCaseImpl(
            CompanyMembershipRepository membershipRepository,
            SecurityContextPort securityContext) {
        this.membershipRepository = membershipRepository;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional(readOnly = true)
    public long execute(UUID companyId) throws ApplicationException {
        if (securityContext.getCurrentUserId().isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        return membershipRepository.countPendingMembershipsByCompanyId(companyId);
    }
}
