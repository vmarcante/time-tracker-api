package com.vmarcante.time_tracker.core.application.company.membership.use_cases;

import java.util.UUID;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.company.membership.in.CountPendingMembershipsUseCase;
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
    public long execute(UUID companyId) throws ApplicationException {
        if (securityContext.getCurrentUserId().isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.pendingByCompany(companyId);
        return membershipRepository.count(companyMembershipFilter);
    }
}
