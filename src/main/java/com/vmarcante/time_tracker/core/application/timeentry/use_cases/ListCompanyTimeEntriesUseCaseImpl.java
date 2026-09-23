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
import com.vmarcante.time_tracker.core.application.timeentry.in.ListCompanyTimeEntriesUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListCompanyTimeEntriesUseCaseImpl implements ListCompanyTimeEntriesUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final CompanyRepository companyRepository;
    private final TimeEntryDetailAssembler assembler;
    private final SecurityContextPort securityContext;

    public ListCompanyTimeEntriesUseCaseImpl(
            TimeEntryRepository timeEntryRepository,
            CompanyRepository companyRepository,
            TimeEntryDetailAssembler assembler,
            SecurityContextPort securityContext) {
        this.timeEntryRepository = timeEntryRepository;
        this.companyRepository = companyRepository;
        this.assembler = assembler;
        this.securityContext = securityContext;
    }

    @Override
    public Page<TimeEntrySummaryOutputDTO> execute(UUID companyId, UUID userId, UUID projectId,
            LocalDateTime from, LocalDateTime to, Pageable pageable) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }
        UUID actorId = currentUserId.get();

        CompanyMembership membership = companyRepository.findMembership(actorId, companyId)
                .filter(m -> Boolean.TRUE.equals(m.getApproved()))
                .orElseThrow(() -> new ApplicationException("company.access.denied", null));

        Page<TimeEntry> entries;
        if (membership.getRole().canManage(CompanyRole.MEMBER)) {
            entries = timeEntryRepository.findActiveByCompanyId(companyId, userId, projectId, from, to, pageable);
        } else {
            entries = timeEntryRepository.findActiveLeadScope(companyId, actorId, userId, projectId, from, to, pageable);
        }

        return assembler.toSummaryPage(entries);
    }
}
