package com.vmarcante.time_tracker.core.application.team.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.in.LeaveTeamUseCase;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LeaveTeamUseCaseImpl implements LeaveTeamUseCase {

    private final TeamRepository teamRepository;
    private final CompanyRepository companyRepository;
    private final ProjectRepository projectRepository;
    private final SecurityContextPort securityContext;

    public LeaveTeamUseCaseImpl(
            TeamRepository teamRepository,
            CompanyRepository companyRepository,
            ProjectRepository projectRepository,
            SecurityContextPort securityContext) {
        this.teamRepository = teamRepository;
        this.companyRepository = companyRepository;
        this.projectRepository = projectRepository;
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

        if (!companyRepository.isMember(userId, companyId)) {
            throw new ApplicationException("company.access.denied", null);
        }

        if (!teamRepository.existsActiveByIdAndCompanyId(teamId, companyId)) {
            throw new ApplicationException("team.not.found", null);
        }

        TeamMembership membership = teamRepository.findMembership(userId, teamId)
                .orElseThrow(() -> new ApplicationException("team.membership.not.found", null));

        membership.setActive(false);
        membership.setUpdatedBy(userId);
        teamRepository.saveMembership(membership);

        projectRepository.deactivateAssignmentsByUserIdAndTeamId(userId, teamId, userId);

        log.info("[Leave Team] User {} left team {} (project assignments deactivated)",
                userId, teamId);
    }
}
