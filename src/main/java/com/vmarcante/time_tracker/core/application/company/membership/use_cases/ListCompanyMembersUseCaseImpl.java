package com.vmarcante.time_tracker.core.application.company.membership.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.base.application.pagination.PageableUtils;
import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.company.membership.dto.input.MembershipFilterInputDTO;
import com.vmarcante.time_tracker.core.application.company.membership.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.company.membership.in.ListCompanyMembersUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListCompanyMembersUseCaseImpl implements ListCompanyMembersUseCase {

    private final CompanyMembershipRepository membershipRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public ListCompanyMembersUseCaseImpl(
            CompanyMembershipRepository membershipRepository,
            PersonRepository personRepository,
            SecurityContextPort securityContext) {
        this.membershipRepository = membershipRepository;
        this.personRepository = personRepository;
        this.securityContext = securityContext;
    }

    @Override
    public PageWrapperDTO<CompanyMemberOutputDTO> execute(UUID companyId, MembershipFilterInputDTO filter)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(currentUserId.get(), companyId);
        if (!membershipRepository.exists(companyMembershipFilter)) {
            throw new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN);
        }

        Pageable pageable = PageableUtils.pageable(filter.getPageNumber(), filter.getPageSize(), filter.getSort());
        CompanyMembershipFilter approvedMembersFilter = CompanyMembershipFilterMapper.approvedByCompany(companyId);
        Page<CompanyMembership> members = membershipRepository
                .search(approvedMembersFilter, pageable);

        if (members.isEmpty()) {
            return PageWrapperDTO.of(members.map(m -> CompanyMemberOutputDTO.from(m, null, null)));
        }

        List<UUID> userIds = members.getContent().stream()
                .flatMap(m -> Stream.of(m.getUserId(), m.getApprovedBy()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, Person> persons = personRepository.findAllByIds(userIds).stream()
                .collect(Collectors.toMap(p -> p.getId(), p -> p));

        return PageWrapperDTO.of(members.map(membership -> {
            Person approver = persons.get(membership.getApprovedBy());
            return CompanyMemberOutputDTO.fromPerson(
                    membership,
                    persons.get(membership.getUserId()),
                    approver != null ? approver.getName() : null,
                    null);
        }));
    }
}
