package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.RequestToJoinCompanyUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;
import com.vmarcante.time_tracker.core.domain.company.event.MembershipRequestCreatedEvent;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RequestToJoinCompanyUseCaseImpl implements RequestToJoinCompanyUseCase {

    private final CompanyRepository companyRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;
    private final ApplicationEventPublisher eventPublisher;

    public RequestToJoinCompanyUseCaseImpl(
            CompanyRepository companyRepository,
            PersonRepository personRepository,
            SecurityContextPort securityContext,
            ApplicationEventPublisher eventPublisher) {
        this.companyRepository = companyRepository;
        this.personRepository = personRepository;
        this.securityContext = securityContext;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public CompanyMemberOutputDTO execute(UUID companyId, String requestReason) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID userId = currentUserId.get();

        companyRepository.findById(companyId)
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .orElseThrow(() -> new ApplicationException("company.not.found", null));

        if (companyRepository.hasAnyActiveMembership(userId, companyId)) {
            throw new ApplicationException("company.member.already.exists", null);
        }

        if (requestReason != null && requestReason.length() > 500) {
            throw new ApplicationException("membership.request.reason.too.long", null);
        }

        CompanyMembership membership = new CompanyMembership();
        membership.setUserId(userId);
        membership.setCompanyId(companyId);
        membership.setRole(CompanyRole.MEMBER);
        membership.setOrigin(MembershipOrigin.REQUEST);
        membership.setApproved(false);
        membership.setRequestReason(requestReason);
        membership.setActive(true);
        membership.setCreatedBy(userId);
        membership.setUpdatedBy(userId);

        CompanyMembership saved = companyRepository.saveMembership(membership);

        String memberName = personRepository.findNameById(userId).orElse(null);

        eventPublisher.publishEvent(new MembershipRequestCreatedEvent(saved.getId(), userId, companyId));

        log.info("[Request To Join] User {} requested to join company {}", userId, companyId);

        return CompanyMemberOutputDTO.from(saved, memberName);
    }
}
