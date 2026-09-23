package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.company.dto.input.CreateCompanyInputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.CreateCompanyOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.CreateCompanyUseCase;
import com.vmarcante.time_tracker.core.application.company.policy.CreateCompanyValidationPolicy;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;
import com.vmarcante.time_tracker.core.domain.company.model.Company;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;
import com.vmarcante.time_tracker.core.domain.user.auth.repository.UserAuthRepository;
import com.vmarcante.time_tracker.core.domain.user.enums.AffiliationStatus;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CreateCompanyUseCaseImpl implements CreateCompanyUseCase {

    private final CompanyRepository companyRepository;
    private final CreateCompanyValidationPolicy validationPolicy;
    private final UserAuthRepository userAuthRepository;
    private final SecurityContextPort securityContext;

    public CreateCompanyUseCaseImpl(
            CompanyRepository companyRepository,
            CreateCompanyValidationPolicy validationPolicy,
            UserAuthRepository userAuthRepository,
            SecurityContextPort securityContext) {
        this.companyRepository = companyRepository;
        this.validationPolicy = validationPolicy;
        this.userAuthRepository = userAuthRepository;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public CreateCompanyOutputDTO execute(CreateCompanyInputDTO input) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID userId = currentUserId.get();

        validationPolicy.validate(input);

        if (companyRepository.existsByDocument(input.document().digits())) {
            throw new ApplicationException("company.document.already.exists", null);
        }

        Company company = new Company();
        company.setLegalName(input.legalName().trim());
        company.setTradeName(input.tradeName());
        company.setDocument(input.document().digits());
        company.setDescription(input.description());
        company.setTimezone(input.timezone() != null ? input.timezone() : "America/Sao_Paulo");
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

        userAuthRepository.findById(userId).ifPresent(userAuth -> {
            userAuth.setAffiliation(AffiliationStatus.COMPANY);
            userAuthRepository.save(userAuth);
        });

        log.info("[Create Company] Company created: {} by user: {}", saved.getId(), userId);
        return CreateCompanyOutputDTO.from(saved);
    }
}
