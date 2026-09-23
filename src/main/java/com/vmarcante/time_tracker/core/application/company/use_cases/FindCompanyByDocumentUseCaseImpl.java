package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyPreviewOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.FindCompanyByDocumentUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;
import com.vmarcante.time_tracker.core.domain.user.auth.repository.UserAuthRepository;
import com.vmarcante.time_tracker.core.domain.user.enums.AffiliationStatus;

@Service
public class FindCompanyByDocumentUseCaseImpl implements FindCompanyByDocumentUseCase {

    private final CompanyRepository companyRepository;
    private final UserAuthRepository userAuthRepository;
    private final SecurityContextPort securityContext;

    public FindCompanyByDocumentUseCaseImpl(
            CompanyRepository companyRepository,
            UserAuthRepository userAuthRepository,
            SecurityContextPort securityContext) {
        this.companyRepository = companyRepository;
        this.userAuthRepository = userAuthRepository;
        this.securityContext = securityContext;
    }

    @Override
    public CompanyPreviewOutputDTO execute(String document) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UserAuth userAuth = userAuthRepository.findById(currentUserId.get())
                .orElseThrow(() -> new ApplicationException("user.not.found", null));

        if (userAuth.getAffiliation() != AffiliationStatus.PENDING) {
            throw new ApplicationException("onboarding.already.completed", null);
        }

        String digits = document == null ? "" : document.replaceAll("\\D", "");

        return companyRepository.findByDocument(digits)
                .filter(company -> Boolean.TRUE.equals(company.getActive()))
                .map(CompanyPreviewOutputDTO::from)
                .orElseThrow(() -> new ApplicationException("company.not.found", null));
    }
}
