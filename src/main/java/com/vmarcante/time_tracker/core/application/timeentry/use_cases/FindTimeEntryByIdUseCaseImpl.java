package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.assembler.TimeEntryDetailAssembler;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntryDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.FindTimeEntryByIdUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class FindTimeEntryByIdUseCaseImpl implements FindTimeEntryByIdUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final CompanyRepository companyRepository;
    private final TimeEntryDetailAssembler assembler;
    private final SecurityContextPort securityContext;

    public FindTimeEntryByIdUseCaseImpl(
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
    public TimeEntryDetailOutputDTO execute(UUID entryId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }
        UUID userId = currentUserId.get();

        TimeEntry entry = timeEntryRepository.findById(entryId)
                .orElseThrow(() -> new ApplicationException("timeentry.not.found", null));

        if (!userId.equals(entry.getUserId()) && !canViewAsHierarchy(userId, entry)) {
            throw new ApplicationException("timeentry.access.denied", null);
        }

        return assembler.toDetail(entry);
    }

    private boolean canViewAsHierarchy(UUID userId, TimeEntry entry) {
        if (entry.getCompanyId() == null) {
            return false;
        }
        Optional<CompanyMembership> membership = companyRepository.findMembership(userId, entry.getCompanyId());
        boolean isManager = membership
                .filter(m -> Boolean.TRUE.equals(m.getApproved()))
                .map(m -> m.getRole().canManage(CompanyRole.MEMBER))
                .orElse(false);
        if (isManager) {
            return true;
        }
        return timeEntryRepository.canLeadViewEntry(entry.getId(), userId);
    }
}
