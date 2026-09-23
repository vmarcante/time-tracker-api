package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.company.dto.input.ChangeMemberRoleInputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.ChangeMemberRoleUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChangeMemberRoleUseCaseImpl implements ChangeMemberRoleUseCase {

    private final CompanyRepository companyRepository;
    private final PersonRepository personRepository;
    private final TeamRepository teamRepository;
    private final SecurityContextPort securityContext;

    public ChangeMemberRoleUseCaseImpl(
            CompanyRepository companyRepository,
            PersonRepository personRepository,
            TeamRepository teamRepository,
            SecurityContextPort securityContext) {
        this.companyRepository = companyRepository;
        this.personRepository = personRepository;
        this.teamRepository = teamRepository;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public CompanyMemberOutputDTO execute(UUID companyId, UUID membershipId, ChangeMemberRoleInputDTO input)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID actorId = currentUserId.get();

        if (input.role() == null) {
            throw new ApplicationException("company.member.role.required", null);
        }

        if (input.role() == CompanyRole.OWNER) {
            throw new ApplicationException("company.member.role.owner.not.assignable", null);
        }

        CompanyMembership actorMembership = companyRepository.findMembership(actorId, companyId)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null));

        CompanyMembership target = companyRepository.findMembershipById(membershipId)
                .filter(m -> m.getCompanyId().equals(companyId))
                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                .filter(m -> Boolean.TRUE.equals(m.getApproved()))
                .orElseThrow(() -> new ApplicationException("company.membership.not.found", null));

        if (target.getUserId().equals(actorId)) {
            throw new ApplicationException("company.member.cannot.change.own.role", null);
        }

        if (!actorMembership.getRole().canManage(target.getRole())
                || !actorMembership.getRole().canManage(input.role())) {
            throw new ApplicationException("company.permission.denied", null);
        }

        target.setRole(input.role());
        target.setUpdatedBy(actorId);

        CompanyMembership saved = companyRepository.saveMembership(target);

        if (input.role() == CompanyRole.MEMBER) {
            teamRepository.demoteLeadsByUserIdAndCompanyId(target.getUserId(), companyId, actorId);
        }

        String memberName = personRepository.findNameById(target.getUserId()).orElse(null);
        String approverName = saved.getApprovedBy() != null
                ? personRepository.findNameById(saved.getApprovedBy()).orElse(null)
                : null;

        log.info("[Change Member Role] Membership {} changed to {} by {}", membershipId, input.role(), actorId);

        return CompanyMemberOutputDTO.from(saved, memberName, approverName);
    }
}
