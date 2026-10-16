package com.vmarcante.time_tracker.core.application.company.overview.orchestrator;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.company.overview.dto.output.CompanyMenuOverviewOutputDTO;
import com.vmarcante.time_tracker.core.application.company.membership.in.CountPendingMembershipsUseCase;
import com.vmarcante.time_tracker.core.application.company.overview.in.GetCompanyMenuOverviewUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class CompanyMenuOverviewOrchestrator implements GetCompanyMenuOverviewUseCase {

    private final CountPendingMembershipsUseCase countPendingMembershipsUseCase;
    private final CompanyMembershipRepository membershipRepository;
    private final SecurityContextPort securityContext;

    public CompanyMenuOverviewOrchestrator(
            CountPendingMembershipsUseCase countPendingMembershipsUseCase,
            CompanyMembershipRepository membershipRepository,
            SecurityContextPort securityContext) {
        this.countPendingMembershipsUseCase = countPendingMembershipsUseCase;
        this.membershipRepository = membershipRepository;
        this.securityContext = securityContext;
    }

    @Override
    public CompanyMenuOverviewOutputDTO execute(UUID companyId) throws ApplicationException {
        assertManagerRole(companyId);

        long pendingMembershipsCount = countPendingMembershipsUseCase.execute(companyId);

        return new CompanyMenuOverviewOutputDTO(pendingMembershipsCount);
    }

    private void assertManagerRole(UUID companyId) throws ApplicationException {
        UUID userId = securityContext.getCurrentUserId()
                .orElseThrow(() -> new ApplicationException("user.authenticated.not", null));

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(userId, companyId);
        CompanyMembership actorMembership = membershipRepository.findOne(companyMembershipFilter)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN));

        if (!actorMembership.getRole().canManage(CompanyRole.MEMBER)) {
            throw new ApplicationException("company.permission.denied", null, HttpStatus.FORBIDDEN);
        }
    }
}
