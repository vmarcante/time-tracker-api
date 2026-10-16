package com.vmarcante.time_tracker.core.application.company.membership.use_cases;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import com.vmarcante.time_tracker.base.domain.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.company.membership.dto.input.InviteMemberInputDTO;
import com.vmarcante.time_tracker.core.application.company.membership.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.company.membership.in.InviteMemberUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.event.CompanyInvitationCreatedEvent;
import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InviteMemberUseCaseImpl implements InviteMemberUseCase {

    private final CompanyMembershipRepository membershipRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;
    private final DomainEventPublisher eventPublisher;

    public InviteMemberUseCaseImpl(
            CompanyMembershipRepository membershipRepository,
            PersonRepository personRepository,
            SecurityContextPort securityContext,
            DomainEventPublisher eventPublisher) {
        this.membershipRepository = membershipRepository;
        this.personRepository = personRepository;
        this.securityContext = securityContext;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public CompanyMemberOutputDTO execute(UUID companyId, InviteMemberInputDTO input) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID actorId = currentUserId.get();

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(actorId, companyId);
        CompanyMembership actorMembership = membershipRepository.findOne(companyMembershipFilter)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN));

        if (input.role() == null) {
            throw new ApplicationException("company.member.role.required", null);
        }

        if (input.role() == CompanyRole.OWNER) {
            throw new ApplicationException("company.member.role.owner.not.assignable", null);
        }

        if (!actorMembership.getRole().canManage(input.role())) {
            throw new ApplicationException("company.permission.denied", null, HttpStatus.FORBIDDEN);
        }

        Person invitee = personRepository.findByEmail(input.email().address())
                .orElseThrow(() -> new ApplicationException("user.not.found", null, HttpStatus.NOT_FOUND));

        if (invitee.getId().equals(actorId)) {
            throw new ApplicationException("company.member.cannot.invite.self", null);
        }

        CompanyMembershipFilter inviteeMembershipFilter = CompanyMembershipFilterMapper.activeMembership(invitee.getId(), companyId);
        if (membershipRepository.exists(inviteeMembershipFilter)) {
            throw new ApplicationException("company.member.already.exists", null, HttpStatus.CONFLICT);
        }

        CompanyMembership membership = new CompanyMembership();
        membership.setUserId(invitee.getId());
        membership.setCompanyId(companyId);
        membership.setRole(input.role());
        membership.setOrigin(MembershipOrigin.INVITE);
        membership.setApproved(false);
        membership.setActive(true);
        membership.setCreatedBy(actorId);
        membership.setUpdatedBy(actorId);

        CompanyMembership saved = membershipRepository.saveMembership(membership);

        eventPublisher.publish(new CompanyInvitationCreatedEvent(
                saved.getId(), invitee.getId(), companyId, actorId));

        log.info("[Invite Member] User {} invited to company {} as {} by {}",
                invitee.getId(), companyId, input.role(), actorId);

        return CompanyMemberOutputDTO.from(saved, invitee.getName());
    }
}
