package com.vmarcante.time_tracker.core.application.company.membership.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
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
import com.vmarcante.time_tracker.core.application.company.membership.in.ListPendingMembershipsUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListPendingMembershipsUseCaseImpl implements ListPendingMembershipsUseCase {

    private final CompanyMembershipRepository membershipRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public ListPendingMembershipsUseCaseImpl(
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
        CompanyMembershipFilter pendingMembershipsFilter = CompanyMembershipFilterMapper.pendingByCompany(companyId);
        Page<CompanyMembership> pending = membershipRepository
                .search(pendingMembershipsFilter, pageable);

        if (pending.isEmpty()) {
            return PageWrapperDTO.of(pending.map(m -> CompanyMemberOutputDTO.from(m, null, null)));
        }

        List<UUID> userIds = pending.getContent().stream()
                .map(m -> m.getUserId())
                .distinct()
                .toList();

        Map<UUID, Person> members = personRepository.findAllByIds(userIds).stream()
                .collect(Collectors.toMap(p -> p.getId(), p -> p));

        return PageWrapperDTO.of(pending.map(membership -> CompanyMemberOutputDTO.fromPerson(
                membership,
                members.get(membership.getUserId()),
                null,
                null)));
    }
}
