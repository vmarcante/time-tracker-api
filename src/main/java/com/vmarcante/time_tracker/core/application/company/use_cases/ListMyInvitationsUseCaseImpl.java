package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyInvitationOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.ListMyInvitationsUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.model.Company;
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
    public List<CompanyInvitationOutputDTO> execute() throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        List<CompanyMembership> invitations = companyRepository.findPendingInvitationsByUserId(currentUserId.get());

        if (invitations.isEmpty()) {
            return List.of();
        }

        List<UUID> companyIds = invitations.stream()
                .map(m -> m.getCompanyId())
                .distinct()
                .toList();

        List<Company> companies = companyRepository.findAllByIds(companyIds);

        Map<UUID, String> companyNames = companies.stream()
                .collect(Collectors.toMap(c -> c.getId(), c -> c.getLegalName()));

        return invitations.stream()
                .map(membership -> CompanyInvitationOutputDTO.from(
                        membership,
                        companyNames.get(membership.getCompanyId())))
                .toList();
    }
}
