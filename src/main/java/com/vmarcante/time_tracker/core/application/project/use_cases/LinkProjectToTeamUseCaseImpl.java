package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.in.LinkProjectToTeamUseCase;
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
public class LinkProjectToTeamUseCaseImpl implements LinkProjectToTeamUseCase {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final CompanyRepository companyRepository;
    private final SecurityContextPort securityContext;

    public LinkProjectToTeamUseCaseImpl(
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

        if (projectRepository.isProjectLinkedToTeam(projectId, teamId)) {
            throw new ApplicationException("project.already.linked", null);
        }

        TeamProject link = new TeamProject();
        link.setProjectId(projectId);
        link.setTeamId(teamId);
        link.setActive(true);
        link.setCreatedBy(actorId);
        link.setUpdatedBy(actorId);

        projectRepository.saveTeamLink(link);

        log.info("[Link Project to Team] Project {} linked to team {} by {}",
                projectId, teamId, actorId);
    }
}
