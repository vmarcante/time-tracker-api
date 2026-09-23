package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.assembler.TimeEntryDetailAssembler;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.UpdateTimeEntryInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntryDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.UpdateTimeEntryUseCase;
import com.vmarcante.time_tracker.core.application.timeentry.policy.TimeEntryValidationPolicy;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UpdateTimeEntryUseCaseImpl implements UpdateTimeEntryUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final TimeEntryValidationPolicy validationPolicy;
    private final TimeEntryDetailAssembler assembler;
    private final SecurityContextPort securityContext;

    public UpdateTimeEntryUseCaseImpl(
            TimeEntryRepository timeEntryRepository,
            TimeEntryValidationPolicy validationPolicy,
            TimeEntryDetailAssembler assembler,
            SecurityContextPort securityContext) {
        this.timeEntryRepository = timeEntryRepository;
        this.validationPolicy = validationPolicy;
        this.assembler = assembler;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public TimeEntryDetailOutputDTO execute(UUID entryId, UpdateTimeEntryInputDTO input) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }
        UUID userId = currentUserId.get();

        validationPolicy.validateName(input.name());

        TimeEntry entry = timeEntryRepository.findById(entryId)
                .filter(e -> userId.equals(e.getUserId()))
                .orElseThrow(() -> new ApplicationException("timeentry.not.found", null));

        entry.setName(input.name().trim());
        entry.setDescription(input.description());
        entry.setUpdatedBy(userId);

        TimeEntry saved = timeEntryRepository.save(entry);

        log.info("[Update Time Entry] Entry {} updated by user {}", saved.getId(), userId);

        return assembler.toDetail(saved);
    }
}
