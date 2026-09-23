package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.company.in.RejectMembershipRequestUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RejectMembershipRequestUseCaseImpl implements RejectMembershipRequestUseCase {

    private final CompanyRepository companyRepository;
    private final SecurityContextPort securityContext;

    public RejectMembershipRequestUseCaseImpl(
            CompanyRepository companyRepository,
            SecurityContextPort securityContext) {
        this.companyRepository = companyRepository;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public void execute(UUID companyId, UUID membershipId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID actorId = currentUserId.get();

        CompanyMembership actorMembership = companyRepository.findMembership(actorId, companyId)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null));

        CompanyMembership target = companyRepository.findMembershipById(membershipId)
                .filter(m -> m.getCompanyId().equals(companyId))
                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                .orElseThrow(() -> new ApplicationException("company.membership.not.found", null));

        if (Boolean.TRUE.equals(target.getApproved())) {
            throw new ApplicationException("company.membership.already.approved", null);
        }

        if (target.getOrigin() != MembershipOrigin.REQUEST) {
            throw new ApplicationException("company.membership.not.request", null);
        }

        if (!actorMembership.getRole().canManage(target.getRole())) {
            throw new ApplicationException("company.permission.denied", null);
        }

        target.setActive(false);
        target.setUpdatedBy(actorId);
        companyRepository.saveMembership(target);

        log.info("[Reject Membership] Membership {} rejected by {}", membershipId, actorId);
    }
}
