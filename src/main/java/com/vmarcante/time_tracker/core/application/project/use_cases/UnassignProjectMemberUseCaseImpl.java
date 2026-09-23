package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.in.UnassignProjectMemberUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.project.model.ProjectAssignment;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UnassignProjectMemberUseCaseImpl implements UnassignProjectMemberUseCase {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final CompanyRepository companyRepository;
    private final SecurityContextPort securityContext;

    public UnassignProjectMemberUseCaseImpl(
            ProjectRepository projectRepository,
            TeamRepository teamRepository,
            CompanyRepository companyRepository,
            SecurityContextPort securityContext) {
        this.projectRepository = projectRepository;
        this.teamRepository = teamRepository;
        this.companyRepository = companyRepository;
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

        CompanyMembership actorMembership = companyRepository.findMembership(actorId, companyId)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null));

        if (!teamRepository.existsActiveByIdAndCompanyId(teamId, companyId)) {
            throw new ApplicationException("team.not.found", null);
        }

        boolean canManage = actorMembership.getRole().canManage(CompanyRole.MANAGER)
                || teamRepository.isTeamLead(actorId, teamId);
        if (!canManage) {
            throw new ApplicationException("project.permission.denied", null);
        }

        ProjectAssignment assignment = projectRepository.findAssignmentById(assignmentId)
                .filter(a -> a.getProjectId().equals(projectId))
                .filter(a -> a.getTeamId().equals(teamId))
                .filter(a -> Boolean.TRUE.equals(a.getActive()))
                .orElseThrow(() -> new ApplicationException("project.assignment.not.found", null));

        assignment.setActive(false);
        assignment.setUpdatedBy(actorId);
        projectRepository.saveAssignment(assignment);

        log.info("[Unassign Project Member] Assignment {} removed from project {} by {}",
                assignmentId, projectId, actorId);
    }
}
