package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.in.UnlinkProjectFromTeamUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.project.model.TeamProject;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UnlinkProjectFromTeamUseCaseImpl implements UnlinkProjectFromTeamUseCase {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final CompanyRepository companyRepository;
    private final SecurityContextPort securityContext;

    public UnlinkProjectFromTeamUseCaseImpl(
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
    public void execute(UUID companyId, UUID projectId, UUID teamId) throws ApplicationException {
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

        if (!projectRepository.existsActiveByIdAndCompanyId(projectId, companyId)) {
            throw new ApplicationException("project.not.found", null);
        }

        if (!teamRepository.existsActiveByIdAndCompanyId(teamId, companyId)) {
            throw new ApplicationException("team.not.found", null);
        }

        TeamProject link = projectRepository.findActiveTeamLink(projectId, teamId)
                .orElseThrow(() -> new ApplicationException("project.not.linked", null));

        projectRepository.deactivateAssignmentsByProjectIdAndTeamId(projectId, teamId, actorId);

        link.setActive(false);
        link.setUpdatedBy(actorId);
        projectRepository.saveTeamLink(link);

        log.info("[Unlink Project from Team] Project {} unlinked from team {} by {} "
                + "(member assignments deactivated)", projectId, teamId, actorId);
    }
}
