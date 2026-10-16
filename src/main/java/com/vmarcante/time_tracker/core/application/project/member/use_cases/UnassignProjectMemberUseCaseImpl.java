package com.vmarcante.time_tracker.core.application.project.member.use_cases;

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
import com.vmarcante.time_tracker.core.application.project.member.in.UnassignProjectMemberUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.project.model.ProjectAssignment;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamMembershipRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilter;
import com.vmarcante.time_tracker.core.domain.team.enums.TeamRole;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UnassignProjectMemberUseCaseImpl implements UnassignProjectMemberUseCase {

    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final TeamRepository teamRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final SecurityContextPort securityContext;

    public UnassignProjectMemberUseCaseImpl(
            ProjectAssignmentRepository projectAssignmentRepository,
            TeamRepository teamRepository,
            TeamMembershipRepository teamMembershipRepository,
            CompanyMembershipRepository membershipRepository,
            SecurityContextPort securityContext) {
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.teamRepository = teamRepository;
        this.teamMembershipRepository = teamMembershipRepository;
        this.membershipRepository = membershipRepository;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public void execute(UUID companyId, UUID teamId, UUID projectId, UUID assignmentId)
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
            throw new ApplicationException("project.permission.denied", null, HttpStatus.FORBIDDEN);
        }

        ProjectAssignment assignment = projectAssignmentRepository.findById(assignmentId)
                .filter(a -> a.getProjectId().equals(projectId))
                .filter(a -> a.getTeamId().equals(teamId))
                .filter(a -> Boolean.TRUE.equals(a.getActive()))
                .orElseThrow(() -> new ApplicationException("project.assignment.not.found", null));

        assignment.setActive(false);
        assignment.setUpdatedBy(actorId);
        projectAssignmentRepository.saveAssignment(assignment);

        log.info("[Unassign Project Member] Assignment {} removed from project {} by {}",
                assignmentId, projectId, actorId);
    }
}
