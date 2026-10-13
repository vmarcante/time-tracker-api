package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.company.dto.input.PreRegisterAndInviteInputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.PreRegisterAndInviteUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;
import com.vmarcante.time_tracker.core.domain.company.event.CompanyInvitationPreRegisteredEvent;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;
import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;
import com.vmarcante.time_tracker.core.shared.vo.Email;
import com.vmarcante.time_tracker.core.shared.vo.Phone;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PreRegisterAndInviteUseCaseImpl implements PreRegisterAndInviteUseCase {

    private final PersonRepository personRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final SecurityContextPort securityContext;
    private final ApplicationEventPublisher eventPublisher;

    public PreRegisterAndInviteUseCaseImpl(
            PersonRepository personRepository,
            CompanyMembershipRepository membershipRepository,
            SecurityContextPort securityContext,
            ApplicationEventPublisher eventPublisher) {
        this.personRepository = personRepository;
        this.membershipRepository = membershipRepository;
        this.securityContext = securityContext;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public CompanyMemberOutputDTO execute(UUID companyId, PreRegisterAndInviteInputDTO input) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID actorId = currentUserId.get();

        CompanyMembership actorMembership = membershipRepository.findMembership(actorId, companyId)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN));

        validateInput(input, actorMembership.getRole());

        if (personRepository.existsByEmail(input.email())) {
            throw new ApplicationException("user.email.unavailable", null, HttpStatus.CONFLICT);
        }

        Person person = buildPerson(input);
        Person saved = personRepository.save(person);

        CompanyMembership membership = new CompanyMembership();
        membership.setUserId(saved.getId());
        membership.setCompanyId(companyId);
        membership.setRole(input.role());
        membership.setOrigin(MembershipOrigin.INVITE);
        membership.setApproved(false);
        membership.setActive(true);
        membership.setCreatedBy(actorId);
        membership.setUpdatedBy(actorId);

        CompanyMembership savedMembership = membershipRepository.saveMembership(membership);

        eventPublisher.publishEvent(new CompanyInvitationPreRegisteredEvent(
                savedMembership.getId(), saved.getId(), companyId, actorId, input.email()));

        log.info("[Pre-Register & Invite] Pre-registered person {} invited to company {} as {} by {}",
                saved.getId(), companyId, input.role(), actorId);

        return CompanyMemberOutputDTO.from(savedMembership, saved.getName());
    }

    private void validateInput(PreRegisterAndInviteInputDTO input, CompanyRole actorRole) throws ApplicationException {
        if (!StringValidationUtils.containsContent(input.name())) {
            throw new ApplicationException("user.name.required", null);
        }

        if (!StringValidationUtils.containsContent(input.email())) {
            throw new ApplicationException("field.email.required", null);
        }

        if (input.role() == null) {
            throw new ApplicationException("company.member.role.required", null);
        }

        if (input.role() == CompanyRole.OWNER) {
            throw new ApplicationException("company.member.role.owner.not.assignable", null);
        }

        if (!actorRole.canManage(input.role())) {
            throw new ApplicationException("company.permission.denied", null, HttpStatus.FORBIDDEN);
        }
    }

    private Person buildPerson(PreRegisterAndInviteInputDTO input) {
        Person person = new Person();
        person.setName(input.name().trim());
        person.setEmail(new Email(input.email()));
        person.setAge(input.age());
        person.setLocale("pt-BR");

        if (StringValidationUtils.containsContent(input.phone())) {
            person.setPhone(new Phone(input.phone()));
        }

        return person;
    }
}
