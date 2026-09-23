package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.company.in.LeaveCompanyUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;
import com.vmarcante.time_tracker.core.domain.user.auth.repository.UserAuthRepository;
import com.vmarcante.time_tracker.core.domain.user.enums.AffiliationStatus;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LeaveCompanyUseCaseImpl implements LeaveCompanyUseCase {

    private final CompanyRepository companyRepository;
    private final TeamRepository teamRepository;
    private final ProjectRepository projectRepository;
    private final UserAuthRepository userAuthRepository;
    private final SecurityContextPort securityContext;

    public LeaveCompanyUseCaseImpl(
            CompanyRepository companyRepository,
            TeamRepository teamRepository,
            ProjectRepository projectRepository,
            UserAuthRepository userAuthRepository,
            SecurityContextPort securityContext) {
        this.companyRepository = companyRepository;
        this.teamRepository = teamRepository;
        this.projectRepository = projectRepository;
        this.userAuthRepository = userAuthRepository;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public void execute(UUID companyId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID userId = currentUserId.get();

        CompanyMembership membership = companyRepository.findMembership(userId, companyId)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null));

        if (membership.getRole() == CompanyRole.OWNER) {
            throw new ApplicationException("company.owner.cannot.leave", null);
        }

        membership.setActive(false);
        membership.setUpdatedBy(userId);
        companyRepository.saveMembership(membership);

        teamRepository.deactivateMembershipsByUserIdAndCompanyId(userId, companyId, userId);
        projectRepository.deactivateAssignmentsByUserIdAndCompanyId(userId, companyId, userId);

        if (!companyRepository.hasAnyApprovedMembership(userId)) {
            userAuthRepository.findById(userId).ifPresent(userAuth -> {
                userAuth.setAffiliation(AffiliationStatus.INDEPENDENT);
                userAuthRepository.save(userAuth);
            });
        }

        log.info("[Leave Company] User {} left company {} "
                + "(team memberships and project assignments deactivated)", userId, companyId);
    }
}
