package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.ListPendingMembershipsUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListPendingMembershipsUseCaseImpl implements ListPendingMembershipsUseCase {

    private final CompanyRepository companyRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public ListPendingMembershipsUseCaseImpl(
            CompanyRepository companyRepository,
            PersonRepository personRepository,
            SecurityContextPort securityContext) {
        this.companyRepository = companyRepository;
        this.personRepository = personRepository;
        this.securityContext = securityContext;
    }

    @Override
    public Page<CompanyMemberOutputDTO> execute(UUID companyId, Pageable pageable)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        CompanyMembership actorMembership = companyRepository.findMembership(currentUserId.get(), companyId)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null));

        if (!actorMembership.getRole().canManage(CompanyRole.MEMBER)) {
            throw new ApplicationException("company.permission.denied", null);
        }

        Page<CompanyMembership> pending = companyRepository
                .findPendingMembershipsByCompanyId(companyId, pageable);

        if (pending.isEmpty()) {
            return pending.map(m -> CompanyMemberOutputDTO.from(m, null, null));
        }

        List<UUID> userIds = pending.getContent().stream()
                .flatMap(m -> Stream.of(m.getUserId(), m.getApprovedBy()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, String> memberNames = personRepository.findNamesByIds(userIds);

        return pending.map(membership -> CompanyMemberOutputDTO.from(
                membership,
                memberNames.get(membership.getUserId()),
                memberNames.get(membership.getApprovedBy())));
    }
}
