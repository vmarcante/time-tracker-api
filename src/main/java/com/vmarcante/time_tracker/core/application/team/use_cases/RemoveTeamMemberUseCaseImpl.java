package com.vmarcante.time_tracker.core.application.team.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.in.RemoveTeamMemberUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RemoveTeamMemberUseCaseImpl implements RemoveTeamMemberUseCase {

    private final TeamRepository teamRepository;
    private final CompanyRepository companyRepository;
    private final ProjectRepository projectRepository;
    private final SecurityContextPort securityContext;

    public RemoveTeamMemberUseCaseImpl(
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
    public void execute(UUID companyId, UUID teamId, UUID membershipId) throws ApplicationException {
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
            throw new ApplicationException("team.permission.denied", null);
        }

        TeamMembership target = teamRepository.findMembershipById(membershipId)
                .filter(m -> m.getTeamId().equals(teamId))
                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                .orElseThrow(() -> new ApplicationException("team.membership.not.found", null));

        target.setActive(false);
        target.setUpdatedBy(actorId);
        teamRepository.saveMembership(target);

        projectRepository.deactivateAssignmentsByUserIdAndTeamId(target.getUserId(), teamId, actorId);

        log.info("[Remove Team Member] Membership {} removed from team {} by {} "
                + "(project assignments deactivated)", membershipId, teamId, actorId);
    }
}
