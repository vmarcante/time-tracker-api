package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.ApproveMembershipRequestUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;
import com.vmarcante.time_tracker.core.domain.user.auth.repository.UserAuthRepository;
import com.vmarcante.time_tracker.core.domain.user.enums.AffiliationStatus;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApproveMembershipRequestUseCaseImpl implements ApproveMembershipRequestUseCase {

    private final CompanyRepository companyRepository;
    private final PersonRepository personRepository;
    private final UserAuthRepository userAuthRepository;
    private final SecurityContextPort securityContext;

    public ApproveMembershipRequestUseCaseImpl(
            CompanyRepository companyRepository,
            PersonRepository personRepository,
            UserAuthRepository userAuthRepository,
            SecurityContextPort securityContext) {
        this.companyRepository = companyRepository;
        this.personRepository = personRepository;
        this.userAuthRepository = userAuthRepository;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public CompanyMemberOutputDTO execute(UUID companyId, UUID membershipId) throws ApplicationException {
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

        if (Boolean.TRUE.equals(target.getApproved())) {
            throw new ApplicationException("company.membership.already.approved", null);
        }

        if (target.getOrigin() != MembershipOrigin.REQUEST) {
            throw new ApplicationException("company.membership.not.request", null);
        }

        if (!actorMembership.getRole().canManage(target.getRole())) {
            throw new ApplicationException("company.permission.denied", null);
        }

        target.setApproved(true);
        target.setApprovedAt(LocalDateTime.now());
        target.setApprovedBy(actorId);
        target.setUpdatedBy(actorId);

        CompanyMembership saved = companyRepository.saveMembership(target);

        userAuthRepository.findById(target.getUserId()).ifPresent(userAuth -> {
            userAuth.setAffiliation(AffiliationStatus.COMPANY);
            userAuthRepository.save(userAuth);
        });

        String memberName = personRepository.findNameById(target.getUserId()).orElse(null);
        String approverName = personRepository.findNameById(actorId).orElse(null);

        log.info("[Approve Membership] Membership {} approved by {}", membershipId, actorId);

        return CompanyMemberOutputDTO.from(saved, memberName, approverName);
    }
}
