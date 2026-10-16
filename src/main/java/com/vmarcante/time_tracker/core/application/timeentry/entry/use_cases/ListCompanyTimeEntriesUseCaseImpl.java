package com.vmarcante.time_tracker.core.application.timeentry.entry.use_cases;

import java.util.Optional;
import java.util.UUID;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.base.application.pagination.PageableUtils;
import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.entry.assembler.TimeEntryDetailAssembler;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.input.CompanyTimeEntriesFilterInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.TimeEntrySummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.in.ListCompanyTimeEntriesUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntryFilter;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListCompanyTimeEntriesUseCaseImpl implements ListCompanyTimeEntriesUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final TimeEntryDetailAssembler assembler;
    private final SecurityContextPort securityContext;

    public ListCompanyTimeEntriesUseCaseImpl(
            TimeEntryRepository timeEntryRepository,
            CompanyMembershipRepository membershipRepository,
            TimeEntryDetailAssembler assembler,
            SecurityContextPort securityContext) {
        this.timeEntryRepository = timeEntryRepository;
        this.membershipRepository = membershipRepository;
        this.assembler = assembler;
        this.securityContext = securityContext;
    }

    @Override
    public PageWrapperDTO<TimeEntrySummaryOutputDTO> execute(UUID companyId, CompanyTimeEntriesFilterInputDTO input)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }
        UUID actorId = currentUserId.get();

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(actorId, companyId);
        CompanyMembership membership = membershipRepository.findOne(companyMembershipFilter)
                .filter(m -> Boolean.TRUE.equals(m.getApproved()))
                .orElseThrow(() -> new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN));

        TimeEntryFilter filter = input.toDomainFilter();
        filter.setCompanyId(companyId);
        if (!membership.getRole().canManage(CompanyRole.MEMBER)) {
            filter.setLeadScopeUserId(actorId);
        }
        Pageable pageable = input.getSort() != null
                ? PageableUtils.pageable(input.getPageNumber(), input.getPageSize(), input.getSort())
                : PageableUtils.unsortedPageable(input.getPageNumber(), input.getPageSize());

        Page<TimeEntry> entries = timeEntryRepository.search(filter, pageable);

        return PageWrapperDTO.of(assembler.toSummaryPage(entries));
    }
}
