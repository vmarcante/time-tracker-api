package com.vmarcante.time_tracker.core.application.user.auth.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.OnboardingInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.output.CompleteOnboardingOutputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.in.CompleteOnboardingUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.event.MembershipRequestCreatedEvent;
import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;
import com.vmarcante.time_tracker.core.domain.company.model.Company;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;
import com.vmarcante.time_tracker.core.domain.user.auth.repository.UserAuthRepository;
import com.vmarcante.time_tracker.core.domain.user.enums.AffiliationStatus;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CompleteOnboardingUseCaseImpl implements CompleteOnboardingUseCase {

    private final UserAuthRepository userAuthRepository;
    private final CompanyRepository companyRepository;
    private final SecurityContextPort securityContext;
    private final ApplicationEventPublisher eventPublisher;

    public CompleteOnboardingUseCaseImpl(
            UserAuthRepository userAuthRepository,
            CompanyRepository companyRepository,
            SecurityContextPort securityContext,
            ApplicationEventPublisher eventPublisher) {
        this.userAuthRepository = userAuthRepository;
        this.companyRepository = companyRepository;
        this.securityContext = securityContext;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public CompleteOnboardingOutputDTO execute(OnboardingInputDTO input) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID userId = currentUserId.get();

        boolean hasDocument = input.document() != null;
        boolean isIndependent = Boolean.TRUE.equals(input.independent());

        if (hasDocument == isIndependent) {
            throw new ApplicationException("onboarding.invalid.choice", null);
        }

        UserAuth userAuth = userAuthRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException("user.not.found", null));

        if (userAuth.getAffiliation() == AffiliationStatus.COMPANY) {
            throw new ApplicationException("onboarding.already.completed", null);
        }

        if (isIndependent) {
            userAuth.setAffiliation(AffiliationStatus.INDEPENDENT);
            userAuthRepository.save(userAuth);

            log.info("[Onboarding] User {} marked as independent", userId);
            return CompleteOnboardingOutputDTO.independent();
        }

        Company company = companyRepository.findByDocument(input.document().digits())
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .orElseThrow(() -> new ApplicationException("company.not.found", null));

        userAuth.setAffiliation(AffiliationStatus.COMPANY);
        userAuthRepository.save(userAuth);

        if (!companyRepository.hasAnyActiveMembership(userId, company.getId())) {
            CompanyMembership membership = new CompanyMembership();
            membership.setUserId(userId);
            membership.setCompanyId(company.getId());
            membership.setRole(CompanyRole.MEMBER);
            membership.setOrigin(MembershipOrigin.REQUEST);
            membership.setApproved(false);
            membership.setActive(true);
            membership.setCreatedBy(userId);
            membership.setUpdatedBy(userId);
            CompanyMembership saved = companyRepository.saveMembership(membership);
            eventPublisher.publishEvent(new MembershipRequestCreatedEvent(
                    saved.getId(), userId, company.getId()));
        }

        boolean membershipPending = !companyRepository.isMember(userId, company.getId());

        log.info("[Onboarding] User {} affiliated with company {} (pending: {})",
                userId, company.getId(), membershipPending);

        return CompleteOnboardingOutputDTO.company(company.getId(), company.getLegalName(), membershipPending);
    }
}
