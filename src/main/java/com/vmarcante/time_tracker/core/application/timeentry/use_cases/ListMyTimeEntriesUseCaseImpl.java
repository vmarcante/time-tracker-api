package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.base.application.pagination.PageableUtils;
import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.assembler.TimeEntryDetailAssembler;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.MyTimeEntriesFilterInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntrySummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.ListMyTimeEntriesUseCase;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntryFilter;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListMyTimeEntriesUseCaseImpl implements ListMyTimeEntriesUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final TimeEntryDetailAssembler assembler;
    private final SecurityContextPort securityContext;

    public ListMyTimeEntriesUseCaseImpl(
            TimeEntryRepository timeEntryRepository,
            TimeEntryDetailAssembler assembler,
            SecurityContextPort securityContext) {
        this.timeEntryRepository = timeEntryRepository;
        this.assembler = assembler;
        this.securityContext = securityContext;
    }

    @Override
    public PageWrapperDTO<TimeEntrySummaryOutputDTO> execute(MyTimeEntriesFilterInputDTO input)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        TimeEntryFilter filter = input.toDomainFilter();
        filter.setUserId(currentUserId.get());
        Pageable pageable = input.getSort() != null
                ? PageableUtils.pageable(input.getPageNumber(), input.getPageSize(), input.getSort())
                : PageableUtils.unsortedPageable(input.getPageNumber(), input.getPageSize());

        Page<TimeEntry> entries = timeEntryRepository.search(filter, pageable);

        return PageWrapperDTO.of(assembler.toSummaryPage(entries));
    }
}
