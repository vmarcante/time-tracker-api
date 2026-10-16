package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import com.vmarcante.time_tracker.base.domain.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.company.dto.input.RejectMembershipInputDTO;
import com.vmarcante.time_tracker.core.application.company.in.RejectMembershipRequestUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;
import com.vmarcante.time_tracker.core.domain.company.event.MembershipRejectedEvent;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RejectMembershipRequestUseCaseImpl implements RejectMembershipRequestUseCase {

    private final CompanyMembershipRepository membershipRepository;
    private final SecurityContextPort securityContext;
    private final DomainEventPublisher eventPublisher;

    public RejectMembershipRequestUseCaseImpl(
            CompanyMembershipRepository membershipRepository,
            SecurityContextPort securityContext,
            DomainEventPublisher eventPublisher) {
        this.membershipRepository = membershipRepository;
        this.securityContext = securityContext;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void execute(UUID companyId, UUID membershipId, RejectMembershipInputDTO input) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID actorId = currentUserId.get();

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(actorId, companyId);
        CompanyMembership actorMembership = membershipRepository.findOne(companyMembershipFilter)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN));

        CompanyMembership target = membershipRepository.findById(membershipId)
                .filter(m -> m.getCompanyId().equals(companyId))
                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                .orElseThrow(() -> new ApplicationException("company.membership.not.found", null));

        if (Boolean.TRUE.equals(target.getApproved())) {
            throw new ApplicationException("company.membership.already.approved", null, HttpStatus.CONFLICT);
        }

        if (target.getOrigin() != MembershipOrigin.REQUEST) {
            throw new ApplicationException("company.membership.not.request", null, HttpStatus.CONFLICT);
        }

        if (!actorMembership.getRole().canManage(target.getRole())) {
            throw new ApplicationException("company.permission.denied", null, HttpStatus.FORBIDDEN);
        }

        String rejectionReason = input != null && input.rejectionReason() != null
                ? input.rejectionReason().trim()
                : null;

        if (rejectionReason != null && rejectionReason.length() > 500) {
            throw new ApplicationException("membership.rejection.reason.too.long", null);
        }

        target.setActive(false);
        target.setRejectionReason(rejectionReason != null && rejectionReason.isEmpty() ? null : rejectionReason);
        target.setUpdatedBy(actorId);
        membershipRepository.saveMembership(target);

        eventPublisher.publish(new MembershipRejectedEvent(
                target.getUserId(), companyId, target.getRejectionReason()));

        log.info("[Reject Membership] Membership {} rejected by {}", membershipId, actorId);
    }
}
