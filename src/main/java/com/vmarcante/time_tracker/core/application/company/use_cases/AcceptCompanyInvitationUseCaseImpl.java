package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.AcceptCompanyInvitationUseCase;
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
public class AcceptCompanyInvitationUseCaseImpl implements AcceptCompanyInvitationUseCase {

    private final CompanyRepository companyRepository;
    private final PersonRepository personRepository;
    private final UserAuthRepository userAuthRepository;
    private final SecurityContextPort securityContext;

    public AcceptCompanyInvitationUseCaseImpl(
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
    public CompanyMemberOutputDTO execute(UUID companyId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID userId = currentUserId.get();

        CompanyMembership membership = companyRepository.findPendingMembership(userId, companyId)
                .filter(m -> m.getOrigin() == MembershipOrigin.INVITE)
                .orElseThrow(() -> new ApplicationException("company.invitation.not.found", null));

        membership.setApproved(true);
        membership.setApprovedAt(LocalDateTime.now());
        membership.setApprovedBy(userId);
        membership.setUpdatedBy(userId);

        CompanyMembership saved = companyRepository.saveMembership(membership);

        userAuthRepository.findById(userId).ifPresent(userAuth -> {
            userAuth.setAffiliation(AffiliationStatus.COMPANY);
            userAuthRepository.save(userAuth);
        });

        String memberName = personRepository.findNameById(userId).orElse(null);

        log.info("[Accept Invitation] User {} accepted invitation to company {}", userId, companyId);

        return CompanyMemberOutputDTO.from(saved, memberName, memberName);
    }
}
