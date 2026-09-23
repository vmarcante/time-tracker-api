package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.company.in.DeactivateCompanyUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.Company;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DeactivateCompanyUseCaseImpl implements DeactivateCompanyUseCase {

    private final CompanyRepository companyRepository;
    private final SecurityContextPort securityContext;

    public DeactivateCompanyUseCaseImpl(
            CompanyRepository companyRepository,
            SecurityContextPort securityContext) {
        this.companyRepository = companyRepository;
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

        Optional<CompanyMembership> membership = companyRepository.findMembership(userId, companyId);
        if (membership.isEmpty() || membership.get().getRole() != CompanyRole.OWNER) {
            throw new ApplicationException("company.owner.required", null);
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ApplicationException("company.not.found", null));

        company.setActive(false);
        company.setUpdatedBy(userId);
        companyRepository.save(company);

        log.info("[Deactivate Company] Company {} deactivated by OWNER: {}", companyId, userId);
    }
}
