package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.company.in.RemoveMemberUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RemoveMemberUseCaseImpl implements RemoveMemberUseCase {

    private final CompanyRepository companyRepository;
    private final TeamRepository teamRepository;
    private final ProjectRepository projectRepository;
    private final SecurityContextPort securityContext;

    public RemoveMemberUseCaseImpl(
            CompanyRepository companyRepository,
            TeamRepository teamRepository,
            ProjectRepository projectRepository,
            SecurityContextPort securityContext) {
        this.companyRepository = companyRepository;
        this.teamRepository = teamRepository;
        this.projectRepository = projectRepository;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public void execute(UUID companyId, UUID membershipId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID actorId = currentUserId.get();

        CompanyMembership actorMembership = companyRepository.findMembership(actorId, companyId)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null));

        CompanyMembership target = companyRepository.findMembershipById(membershipId)
                .filter(m -> m.getCompanyId().equals(companyId))
                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                .orElseThrow(() -> new ApplicationException("company.membership.not.found", null));

        if (target.getUserId().equals(actorId)) {
            throw new ApplicationException("company.member.cannot.remove.self", null);
        }

        if (!actorMembership.getRole().canManage(target.getRole())) {
            throw new ApplicationException("company.permission.denied", null);
        }

        target.setActive(false);
        target.setUpdatedBy(actorId);
        companyRepository.saveMembership(target);

        teamRepository.deactivateMembershipsByUserIdAndCompanyId(target.getUserId(), companyId, actorId);
        projectRepository.deactivateAssignmentsByUserIdAndCompanyId(target.getUserId(), companyId, actorId);

        log.info("[Remove Member] Membership {} removed by {} "
                + "(team memberships and project assignments deactivated)", membershipId, actorId);
    }
}
