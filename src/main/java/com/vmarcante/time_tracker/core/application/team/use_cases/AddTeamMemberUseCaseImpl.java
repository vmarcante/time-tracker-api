package com.vmarcante.time_tracker.core.application.team.use_cases;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamMembershipFilter;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.input.TeamMemberEmailInputDTO;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.team.in.AddTeamMemberUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.team.enums.TeamRole;
import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilter;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamMembershipRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AddTeamMemberUseCaseImpl implements AddTeamMemberUseCase {

    private final TeamRepository teamRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public AddTeamMemberUseCaseImpl(
            TeamRepository teamRepository,
            TeamMembershipRepository teamMembershipRepository,
            CompanyMembershipRepository membershipRepository,
            PersonRepository personRepository,
            SecurityContextPort securityContext) {
        this.teamRepository = teamRepository;
        this.teamMembershipRepository = teamMembershipRepository;
        this.membershipRepository = membershipRepository;
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

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(actorId, companyId);
        CompanyMembership actorMembership = membershipRepository.findOne(companyMembershipFilter)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN));

        TeamFilter teamFilter = TeamFilterMapper.activeByIdAndCompanyId(teamId, companyId);
        if (!teamRepository.exists(teamFilter)) {
            throw new ApplicationException("team.not.found", null, HttpStatus.NOT_FOUND);
        }

        TeamMembershipFilter teamMembershipFilter = TeamMembershipFilterMapper.approvedMembershipWithRole(actorId, teamId, TeamRole.LEAD);
        boolean canManage = actorMembership.getRole().canManage(CompanyRole.MANAGER)
                || teamMembershipRepository.exists(teamMembershipFilter);
        if (!canManage) {
            throw new ApplicationException("team.permission.denied", null, HttpStatus.FORBIDDEN);
        }

        Person target = personRepository.findByEmail(input.email().address())
                .orElseThrow(() -> new ApplicationException("user.not.found", null, HttpStatus.NOT_FOUND));

        CompanyMembershipFilter targetMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(target.getId(), companyId);
        membershipRepository.findOne(targetMembershipFilter)
                .orElseThrow(() -> new ApplicationException("team.member.not.company.member", null));

        TeamMembershipFilter existingMembershipFilter = TeamMembershipFilterMapper.activeMembership(target.getId(), teamId);
        if (teamMembershipRepository.exists(existingMembershipFilter)) {
            throw new ApplicationException("team.member.already.exists", null, HttpStatus.CONFLICT);
        }

        TeamMembership membership = new TeamMembership();
        membership.setUserId(target.getId());
        membership.setTeamId(teamId);
        membership.setRole(TeamRole.MEMBER);
        membership.setApproved(true);
        membership.setActive(true);
        membership.setCreatedBy(actorId);
        membership.setUpdatedBy(actorId);

        TeamMembership saved = teamMembershipRepository.saveMembership(membership);

        log.info("[Add Team Member] User {} added to team {} by {}", target.getId(), teamId, actorId);

        return TeamMemberOutputDTO.from(saved, target.getName());
    }
}
