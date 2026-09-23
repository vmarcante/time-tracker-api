package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.company.dto.input.UpdateCompanyInputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.UpdateCompanyUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.Company;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UpdateCompanyUseCaseImpl implements UpdateCompanyUseCase {

    private final CompanyRepository companyRepository;
    private final SecurityContextPort securityContext;

    public UpdateCompanyUseCaseImpl(
            CompanyRepository companyRepository,
            SecurityContextPort securityContext) {
        this.companyRepository = companyRepository;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public CompanyDetailOutputDTO execute(UUID companyId, UpdateCompanyInputDTO input) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID userId = currentUserId.get();

        Optional<CompanyMembership> membership = companyRepository.findMembership(userId, companyId);
        if (membership.isEmpty()) {
            throw new ApplicationException("company.access.denied", null);
        }

        CompanyRole role = membership.get().getRole();
        if (role != CompanyRole.OWNER && role != CompanyRole.MANAGER) {
            throw new ApplicationException("company.permission.denied", null);
        }

        Company company = companyRepository.findById(companyId)
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .orElseThrow(() -> new ApplicationException("company.not.found", null));

        if (input.legalName() != null && !input.legalName().isBlank()) {
            company.setLegalName(input.legalName().trim());
        }
        if (input.tradeName() != null) {
            company.setTradeName(input.tradeName().isBlank() ? null : input.tradeName().trim());
        }
        if (input.description() != null) {
            company.setDescription(input.description());
        }
        if (input.timezone() != null && !input.timezone().isBlank()) {
            company.setTimezone(input.timezone());
        }
        company.setUpdatedBy(userId);

        log.info("[Update Company] Company {} updated by user: {}", companyId, userId);
        return CompanyDetailOutputDTO.from(companyRepository.save(company));
    }
}
