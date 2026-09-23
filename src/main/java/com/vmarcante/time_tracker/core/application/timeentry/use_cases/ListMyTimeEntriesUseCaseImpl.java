package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.assembler.TimeEntryDetailAssembler;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntrySummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.ListMyTimeEntriesUseCase;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
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
    public Page<TimeEntrySummaryOutputDTO> execute(UUID projectId, LocalDateTime from, LocalDateTime to,
            Pageable pageable) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        Page<TimeEntry> entries = timeEntryRepository.findActiveByUserId(
                currentUserId.get(), projectId, from, to, pageable);

        return assembler.toSummaryPage(entries);
    }
}
