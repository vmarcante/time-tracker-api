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
import com.vmarcante.time_tracker.core.application.company.membership.in.ListResolvedMembershipsUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListResolvedMembershipsUseCaseImpl implements ListResolvedMembershipsUseCase {

    private final CompanyMembershipRepository membershipRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public ListResolvedMembershipsUseCaseImpl(
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
        CompanyMembership actorMembership = membershipRepository.findOne(companyMembershipFilter)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN));

        if (!actorMembership.getRole().canManage(CompanyRole.MEMBER)) {
            throw new ApplicationException("company.permission.denied", null, HttpStatus.FORBIDDEN);
        }

        Pageable pageable = PageableUtils.pageable(filter.getPageNumber(), filter.getPageSize(), filter.getSort());
        CompanyMembershipFilter resolvedMembershipsFilter = CompanyMembershipFilterMapper.resolvedByCompany(companyId);
        Page<CompanyMembership> resolved = membershipRepository
                .search(resolvedMembershipsFilter, pageable);

        if (resolved.isEmpty()) {
            return PageWrapperDTO.of(resolved.map(m -> CompanyMemberOutputDTO.from(m, null, null)));
        }

        List<UUID> personIds = resolved.getContent().stream()
                .flatMap(m -> Stream.of(m.getUserId(), m.getApprovedBy(), m.getUpdatedBy()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, Person> persons = personRepository.findAllByIds(personIds).stream()
                .collect(Collectors.toMap(p -> p.getId(), p -> p));

        return PageWrapperDTO.of(resolved.map(membership -> {
            Person member = persons.get(membership.getUserId());
            String approverName = persons.containsKey(membership.getApprovedBy())
                    ? persons.get(membership.getApprovedBy()).getName()
                    : null;
            String updaterName = persons.containsKey(membership.getUpdatedBy())
                    ? persons.get(membership.getUpdatedBy()).getName()
                    : null;
            return CompanyMemberOutputDTO.fromPerson(membership, member, approverName, updaterName);
        }));
    }
}
