package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.company.dto.output.PersonInviteLookupOutputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.PersonInviteLookupStatus;
import com.vmarcante.time_tracker.core.application.company.in.LookupPersonForInviteUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LookupPersonForInviteUseCaseImpl implements LookupPersonForInviteUseCase {

    private final PersonRepository personRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final SecurityContextPort securityContext;

    public LookupPersonForInviteUseCaseImpl(
            PersonRepository personRepository,
            CompanyMembershipRepository membershipRepository,
            SecurityContextPort securityContext) {
        this.personRepository = personRepository;
        this.membershipRepository = membershipRepository;
        this.securityContext = securityContext;
    }

    @Override
    public PersonInviteLookupOutputDTO execute(UUID companyId, String email) throws ApplicationException {
        UUID actorId = securityContext.getCurrentUserId()
                .orElseThrow(() -> new ApplicationException("user.authenticated.not", null));

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(actorId, companyId);
        CompanyMembership actorMembership = membershipRepository.findOne(companyMembershipFilter)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN));

        // Only roles that can manage at least MEMBER (MANAGER, ADMIN, OWNER) may lookup invitees
        if (!actorMembership.getRole().canManage(CompanyRole.MEMBER)) {
            throw new ApplicationException("company.permission.denied", null, HttpStatus.FORBIDDEN);
        }

        Optional<Person> found = personRepository.findByEmail(email);
        if (found.isEmpty()) {
            return PersonInviteLookupOutputDTO.notFound();
        }

        Person person = found.get();
        UUID personId = person.getId();

        CompanyMembershipFilter existingMembershipFilter = CompanyMembershipFilterMapper.activeMembership(personId, companyId);
        if (membershipRepository.exists(existingMembershipFilter)) {
            return PersonInviteLookupOutputDTO.of(
                    PersonInviteLookupStatus.IN_THIS_COMPANY,
                    personId, person.getName(), person.getEmail().address());
        }

        CompanyMembershipFilter approvedMembershipFilter = CompanyMembershipFilterMapper.approvedByUser(personId);
        if (membershipRepository.exists(approvedMembershipFilter)) {
            return PersonInviteLookupOutputDTO.of(
                    PersonInviteLookupStatus.IN_ANOTHER_COMPANY,
                    personId, person.getName(), person.getEmail().address());
        }

        return PersonInviteLookupOutputDTO.of(
                PersonInviteLookupStatus.AVAILABLE,
                personId, person.getName(), person.getEmail().address());
    }
}
