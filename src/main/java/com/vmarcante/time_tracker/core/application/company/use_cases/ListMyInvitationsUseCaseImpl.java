package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyInvitationOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.ListMyInvitationsUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListMyInvitationsUseCaseImpl implements ListMyInvitationsUseCase {

    private final CompanyRepository companyRepository;
    private final SecurityContextPort securityContext;

    public ListMyInvitationsUseCaseImpl(
            CompanyRepository companyRepository,
            SecurityContextPort securityContext) {
        this.companyRepository = companyRepository;
        this.securityContext = securityContext;
    }

    @Override
    public Page<CompanyInvitationOutputDTO> execute(Pageable pageable) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        Page<CompanyMembership> invitations = companyRepository
                .findPendingInvitationsByUserId(currentUserId.get(), pageable);

        if (invitations.isEmpty()) {
            return invitations.map(m -> CompanyInvitationOutputDTO.from(m, null));
        }

        List<UUID> companyIds = invitations.getContent().stream()
                .map(m -> m.getCompanyId())
                .distinct()
                .toList();

        Map<UUID, String> companyNames = companyRepository.findLegalNamesByIds(companyIds);

        return invitations.map(membership -> CompanyInvitationOutputDTO.from(
                membership,
                companyNames.get(membership.getCompanyId())));
    }
}
