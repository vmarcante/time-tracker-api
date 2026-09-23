package com.vmarcante.time_tracker.core.application.team.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.input.TeamMemberEmailInputDTO;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.team.in.AssignTeamLeadUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.team.enums.TeamRole;
import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AssignTeamLeadUseCaseImpl implements AssignTeamLeadUseCase {

    private final TeamRepository teamRepository;
    private final CompanyRepository companyRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public AssignTeamLeadUseCaseImpl(
            TeamRepository teamRepository,
            CompanyRepository companyRepository,
            PersonRepository personRepository,
            SecurityContextPort securityContext) {
        this.teamRepository = teamRepository;
        this.companyRepository = companyRepository;
        this.personRepository = personRepository;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public TeamMemberOutputDTO execute(UUID companyId, UUID teamId, TeamMemberEmailInputDTO input)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID actorId = currentUserId.get();

        CompanyMembership actorMembership = companyRepository.findMembership(actorId, companyId)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null));

        if (!actorMembership.getRole().canManage(CompanyRole.MANAGER)) {
            throw new ApplicationException("company.permission.denied", null);
        }

        teamRepository.findById(teamId)
                .filter(t -> t.getCompanyId().equals(companyId))
                .filter(t -> Boolean.TRUE.equals(t.getActive()))
                .orElseThrow(() -> new ApplicationException("team.not.found", null));

        Person target = personRepository.findByEmail(input.email().address())
                .orElseThrow(() -> new ApplicationException("user.not.found", null));

        CompanyMembership targetCompanyMembership = companyRepository
                .findMembership(target.getId(), companyId)
                .orElseThrow(() -> new ApplicationException("team.member.not.company.member", null));

        if (targetCompanyMembership.getRole() != CompanyRole.MANAGER) {
            throw new ApplicationException("team.lead.must.be.manager", null);
        }

        if (teamRepository.isTeamLead(target.getId(), teamId)) {
            throw new ApplicationException("team.lead.already.assigned", null);
        }

        Optional<TeamMembership> previousLead = teamRepository.findLeadMembership(teamId);

        TeamMembership membership;
        Optional<TeamMembership> existing = teamRepository.findMembership(target.getId(), teamId);
        if (existing.isPresent()) {
            membership = existing.get();
        } else {
            membership = new TeamMembership();
            membership.setUserId(target.getId());
            membership.setTeamId(teamId);
            membership.setApproved(true);
            membership.setActive(true);
            membership.setCreatedBy(actorId);
        }
        membership.setRole(TeamRole.LEAD);
        membership.setUpdatedBy(actorId);

        TeamMembership saved = teamRepository.saveMembership(membership);

        if (previousLead.isPresent()) {
            TeamMembership demoted = previousLead.get();
            demoted.setRole(TeamRole.MEMBER);
            demoted.setUpdatedBy(actorId);
            teamRepository.saveMembership(demoted);
        }

        log.info("[Assign Team Lead] User {} assigned as lead of team {} by {}",
                target.getId(), teamId, actorId);

        return TeamMemberOutputDTO.from(saved, target.getName());
    }
}
