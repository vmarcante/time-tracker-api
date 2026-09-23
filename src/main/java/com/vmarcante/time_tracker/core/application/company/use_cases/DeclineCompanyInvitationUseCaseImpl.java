package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.company.in.DeclineCompanyInvitationUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DeclineCompanyInvitationUseCaseImpl implements DeclineCompanyInvitationUseCase {

    private final CompanyRepository companyRepository;
    private final SecurityContextPort securityContext;

    public DeclineCompanyInvitationUseCaseImpl(
            CompanyRepository companyRepository,
            SecurityContextPort securityContext) {
        this.companyRepository = companyRepository;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public void execute(UUID companyId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID userId = currentUserId.get();

        CompanyMembership membership = companyRepository.findPendingMembership(userId, companyId)
                .filter(m -> m.getOrigin() == MembershipOrigin.INVITE)
                .orElseThrow(() -> new ApplicationException("company.invitation.not.found", null));

        membership.setActive(false);
        membership.setUpdatedBy(userId);
        companyRepository.saveMembership(membership);

        log.info("[Decline Invitation] User {} declined invitation to company {}", userId, companyId);
    }
}
