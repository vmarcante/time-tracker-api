package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.company.in.LeaveCompanyUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamMembershipRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;
import com.vmarcante.time_tracker.core.domain.user.auth.repository.UserAuthRepository;
import com.vmarcante.time_tracker.core.domain.user.enums.AffiliationStatus;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LeaveCompanyUseCaseImpl implements LeaveCompanyUseCase {

    private final CompanyMembershipRepository membershipRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final UserAuthRepository userAuthRepository;
    private final SecurityContextPort securityContext;

    public LeaveCompanyUseCaseImpl(
            CompanyMembershipRepository membershipRepository,
            TeamMembershipRepository teamMembershipRepository,
            ProjectAssignmentRepository projectAssignmentRepository,
            UserAuthRepository userAuthRepository,
            SecurityContextPort securityContext) {
        this.membershipRepository = membershipRepository;
        this.teamMembershipRepository = teamMembershipRepository;
        this.projectAssignmentRepository = projectAssignmentRepository;
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

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(userId, companyId);
        CompanyMembership membership = membershipRepository.findOne(companyMembershipFilter)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN));

        if (membership.getRole() == CompanyRole.OWNER) {
            throw new ApplicationException("company.owner.cannot.leave", null);
        }

        membership.setActive(false);
        membership.setUpdatedBy(userId);
        membershipRepository.saveMembership(membership);

        teamMembershipRepository.deactivateMembershipsByUserIdAndCompanyId(userId, companyId, userId);
        projectAssignmentRepository.deactivateAssignmentsByUserIdAndCompanyId(userId, companyId, userId);

        CompanyMembershipFilter currentMembershipFilter = CompanyMembershipFilterMapper.approvedByUser(userId);
        if (!membershipRepository.exists(currentMembershipFilter)) {
            userAuthRepository.findById(userId).ifPresent(userAuth -> {
                userAuth.setAffiliation(AffiliationStatus.INDEPENDENT);
                userAuthRepository.save(userAuth);
            });
        }

        log.info("[Leave Company] User {} left company {} "
                + "(team memberships and project assignments deactivated)", userId, companyId);
    }
}
