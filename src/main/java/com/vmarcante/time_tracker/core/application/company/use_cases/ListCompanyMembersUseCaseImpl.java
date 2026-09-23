package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.ListCompanyMembersUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListCompanyMembersUseCaseImpl implements ListCompanyMembersUseCase {

    private final CompanyRepository companyRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public ListCompanyMembersUseCaseImpl(
            CompanyRepository companyRepository,
            PersonRepository personRepository,
            SecurityContextPort securityContext) {
        this.companyRepository = companyRepository;
        this.personRepository = personRepository;
        this.securityContext = securityContext;
    }

    @Override
    public List<CompanyMemberOutputDTO> execute(UUID companyId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        if (!companyRepository.isMember(currentUserId.get(), companyId)) {
            throw new ApplicationException("company.access.denied", null);
        }

        List<CompanyMembership> members = companyRepository.findApprovedMembershipsByCompanyId(companyId);

        List<UUID> userIds = members.stream()
                .flatMap(m -> Stream.of(m.getUserId(), m.getApprovedBy()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, String> memberNames = personRepository.findNamesByIds(userIds);

        return members.stream()
                .map(membership -> CompanyMemberOutputDTO.from(
                        membership,
                        memberNames.get(membership.getUserId()),
                        memberNames.get(membership.getApprovedBy())))
                .toList();
    }
}
