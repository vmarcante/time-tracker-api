package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.company.dto.input.InviteMemberInputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.InviteMemberUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InviteMemberUseCaseImpl implements InviteMemberUseCase {

    private final CompanyRepository companyRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public InviteMemberUseCaseImpl(
            CompanyRepository companyRepository,
            PersonRepository personRepository,
            SecurityContextPort securityContext) {
        this.companyRepository = companyRepository;
        this.personRepository = personRepository;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public CompanyMemberOutputDTO execute(UUID companyId, InviteMemberInputDTO input) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID actorId = currentUserId.get();

        CompanyMembership actorMembership = companyRepository.findMembership(actorId, companyId)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null));

        if (input.role() == null) {
            throw new ApplicationException("company.member.role.required", null);
        }

        if (input.role() == CompanyRole.OWNER) {
            throw new ApplicationException("company.member.role.owner.not.assignable", null);
        }

        if (!actorMembership.getRole().canManage(input.role())) {
            throw new ApplicationException("company.permission.denied", null);
        }

        Person invitee = personRepository.findByEmail(input.email().address())
                .orElseThrow(() -> new ApplicationException("user.not.found", null));

        if (invitee.getId().equals(actorId)) {
            throw new ApplicationException("company.member.cannot.invite.self", null);
        }

        if (companyRepository.hasAnyActiveMembership(invitee.getId(), companyId)) {
            throw new ApplicationException("company.member.already.exists", null);
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

        CompanyMembership saved = companyRepository.saveMembership(membership);

        log.info("[Invite Member] User {} invited to company {} as {} by {}",
                invitee.getId(), companyId, input.role(), actorId);

        return CompanyMemberOutputDTO.from(saved, invitee.getName());
    }
}
