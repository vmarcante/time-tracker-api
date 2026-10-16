package com.vmarcante.time_tracker.core.application.team.membership.use_cases;

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
import com.vmarcante.time_tracker.core.application.team.membership.in.LeaveTeamUseCase;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamMembershipRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilter;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LeaveTeamUseCaseImpl implements LeaveTeamUseCase {

    private final TeamRepository teamRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final SecurityContextPort securityContext;

    public LeaveTeamUseCaseImpl(
            TeamRepository teamRepository,
            TeamMembershipRepository teamMembershipRepository,
            CompanyMembershipRepository membershipRepository,
            ProjectAssignmentRepository projectAssignmentRepository,
            SecurityContextPort securityContext) {
        this.teamRepository = teamRepository;
        this.teamMembershipRepository = teamMembershipRepository;
        this.membershipRepository = membershipRepository;
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public void execute(UUID companyId, UUID teamId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID userId = currentUserId.get();

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(userId, companyId);
        if (!membershipRepository.exists(companyMembershipFilter)) {
            throw new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN);
        }

        TeamFilter teamFilter = TeamFilterMapper.activeByIdAndCompanyId(teamId, companyId);
        if (!teamRepository.exists(teamFilter)) {
            throw new ApplicationException("team.not.found", null, HttpStatus.NOT_FOUND);
        }

        TeamMembershipFilter teamMembershipFilter = TeamMembershipFilterMapper.approvedMembership(userId, teamId);
        TeamMembership membership = teamMembershipRepository.findOne(teamMembershipFilter)
                .orElseThrow(() -> new ApplicationException("team.membership.not.found", null));

        membership.setActive(false);
        membership.setUpdatedBy(userId);
        teamMembershipRepository.saveMembership(membership);

        projectAssignmentRepository.deactivateAssignmentsByUserIdAndTeamId(userId, teamId, userId);

        log.info("[Leave Team] User {} left team {} (project assignments deactivated)",
                userId, teamId);
    }
}
