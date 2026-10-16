package com.vmarcante.time_tracker.core.application.company.membership.use_cases;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.company.membership.in.RemoveMemberUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
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
public class RemoveMemberUseCaseImpl implements RemoveMemberUseCase {

    private final CompanyMembershipRepository membershipRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final UserAuthRepository userAuthRepository;
    private final SecurityContextPort securityContext;

    public RemoveMemberUseCaseImpl(
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
    public void execute(UUID companyId, UUID membershipId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID actorId = currentUserId.get();

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(actorId, companyId);
        CompanyMembership actorMembership = membershipRepository.findOne(companyMembershipFilter)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN));

        CompanyMembership target = membershipRepository.findById(membershipId)
                .filter(m -> m.getCompanyId().equals(companyId))
                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                .orElseThrow(() -> new ApplicationException("company.membership.not.found", null));

        if (target.getUserId().equals(actorId)) {
            throw new ApplicationException("company.member.cannot.remove.self", null);
        }

        if (!actorMembership.getRole().canManage(target.getRole())) {
            throw new ApplicationException("company.permission.denied", null, HttpStatus.FORBIDDEN);
        }

        target.setActive(false);
        target.setUpdatedBy(actorId);
        membershipRepository.saveMembership(target);

        teamMembershipRepository.deactivateMembershipsByUserIdAndCompanyId(target.getUserId(), companyId, actorId);
        projectAssignmentRepository.deactivateAssignmentsByUserIdAndCompanyId(target.getUserId(), companyId, actorId);

        CompanyMembershipFilter targetMembershipFilter = CompanyMembershipFilterMapper.approvedByUser(target.getUserId());
        if (!membershipRepository.exists(targetMembershipFilter)) {
            userAuthRepository.findById(target.getUserId()).ifPresent(userAuth -> {
                userAuth.setAffiliation(AffiliationStatus.INDEPENDENT);
                userAuthRepository.save(userAuth);
            });
        }

        log.info("[Remove Member] Membership {} removed by {} "
                + "(team memberships and project assignments deactivated)", membershipId, actorId);
    }
}
