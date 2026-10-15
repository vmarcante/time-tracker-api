package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.in.UnassignProjectMemberUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.project.model.ProjectAssignment;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamMembershipRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
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

        CompanyMembership actorMembership = membershipRepository.findMembership(actorId, companyId)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN));

        if (!teamRepository.existsActiveByIdAndCompanyId(teamId, companyId)) {
            throw new ApplicationException("team.not.found", null, HttpStatus.NOT_FOUND);
        }

        boolean canManage = actorMembership.getRole().canManage(CompanyRole.MANAGER)
                || teamMembershipRepository.isTeamLead(actorId, teamId);
        if (!canManage) {
            throw new ApplicationException("project.permission.denied", null, HttpStatus.FORBIDDEN);
        }

        ProjectAssignment assignment = projectAssignmentRepository.findAssignmentById(assignmentId)
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
