package com.vmarcante.time_tracker.core.application.user.auth.use_cases;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.company.dto.input.CreateCompanyInputDTO;
import com.vmarcante.time_tracker.core.application.company.policy.CreateCompanyValidationPolicy;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.OnboardingCompanyInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.output.CompleteOnboardingOutputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.in.CreateOnboardingCompanyUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
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
public class CreateOnboardingCompanyUseCaseImpl implements CreateOnboardingCompanyUseCase {

    private final UserAuthRepository userAuthRepository;
    private final CompanyRepository companyRepository;
    private final SecurityContextPort securityContext;
    private final CreateCompanyValidationPolicy validationPolicy;

    public CreateOnboardingCompanyUseCaseImpl(
            UserAuthRepository userAuthRepository,
            CompanyRepository companyRepository,
            SecurityContextPort securityContext,
            CreateCompanyValidationPolicy validationPolicy) {
        this.userAuthRepository = userAuthRepository;
        this.companyRepository = companyRepository;
        this.securityContext = securityContext;
        this.validationPolicy = validationPolicy;
    }

    @Override
    @Transactional
    public CompleteOnboardingOutputDTO execute(OnboardingCompanyInputDTO input) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID userId = currentUserId.get();

        validationPolicy.validate(new CreateCompanyInputDTO(
                input.legalName(), input.tradeName(), input.document(), null, null));

        UserAuth userAuth = userAuthRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException("user.not.found", null));

        if (userAuth.getAffiliation() != AffiliationStatus.PENDING) {
            throw new ApplicationException("onboarding.already.completed", null);
        }

        if (companyRepository.existsByDocument(input.document().digits())) {
            throw new ApplicationException("company.document.already.exists", null);
        }

        Company company = new Company();
        company.setLegalName(input.legalName().trim());
        company.setTradeName(input.tradeName());
        company.setDocument(input.document().digits());
        company.setTimezone("America/Sao_Paulo");
        company.setActive(true);
        company.setCreatedBy(userId);
        company.setUpdatedBy(userId);

        Company saved = companyRepository.save(company);

        CompanyMembership membership = new CompanyMembership();
        membership.setUserId(userId);
        membership.setCompanyId(saved.getId());
        membership.setRole(CompanyRole.OWNER);
        membership.setOrigin(MembershipOrigin.REQUEST);
        membership.setApproved(true);
        membership.setApprovedAt(LocalDateTime.now());
        membership.setApprovedBy(userId);
        membership.setActive(true);
        membership.setCreatedBy(userId);
        membership.setUpdatedBy(userId);

        companyRepository.saveMembership(membership);

        userAuth.setAffiliation(AffiliationStatus.COMPANY);
        userAuthRepository.save(userAuth);

        log.info("[Onboarding] User {} created company {} as OWNER", userId, saved.getId());

        return CompleteOnboardingOutputDTO.company(saved.getId(), saved.getLegalName(), false);
    }
}
