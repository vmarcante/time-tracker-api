package com.vmarcante.time_tracker.core.application.team.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.in.DeactivateTeamUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.model.Team;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DeactivateTeamUseCaseImpl implements DeactivateTeamUseCase {

    private final TeamRepository teamRepository;
    private final CompanyRepository companyRepository;
    private final ProjectRepository projectRepository;
    private final SecurityContextPort securityContext;

    public DeactivateTeamUseCaseImpl(
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

        UUID actorId = currentUserId.get();

        CompanyMembership actorMembership = companyRepository.findMembership(actorId, companyId)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null));

        if (!actorMembership.getRole().canManage(CompanyRole.MANAGER)) {
            throw new ApplicationException("company.permission.denied", null);
        }

        Team team = teamRepository.findById(teamId)
                .filter(t -> t.getCompanyId().equals(companyId))
                .filter(t -> Boolean.TRUE.equals(t.getActive()))
                .orElseThrow(() -> new ApplicationException("team.not.found", null));

        team.setActive(false);
        team.setUpdatedBy(actorId);
        teamRepository.save(team);

        teamRepository.deactivateMembershipsByTeamId(teamId, actorId);
        projectRepository.deactivateAssignmentsByTeamId(teamId, actorId);
        projectRepository.deactivateTeamLinksByTeamId(teamId, actorId);

        log.info("[Deactivate Team] Team {} deactivated by {} "
                + "(memberships, project links and assignments deactivated)", teamId, actorId);
    }
}
