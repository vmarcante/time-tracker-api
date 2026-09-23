package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.assembler.TimeEntryDetailAssembler;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntryDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.DeleteSessionUseCase;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntrySessionRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DeleteSessionUseCaseImpl implements DeleteSessionUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final TimeEntrySessionRepository sessionRepository;
    private final TimeEntryDetailAssembler assembler;
    private final SecurityContextPort securityContext;

    public DeleteSessionUseCaseImpl(
            TimeEntryRepository timeEntryRepository,
            TimeEntrySessionRepository sessionRepository,
            TimeEntryDetailAssembler assembler,
            SecurityContextPort securityContext) {
        this.timeEntryRepository = timeEntryRepository;
        this.sessionRepository = sessionRepository;
        this.assembler = assembler;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public TimeEntryDetailOutputDTO execute(UUID entryId, UUID sessionId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }
        UUID userId = currentUserId.get();

        TimeEntry entry = timeEntryRepository.findById(entryId)
                .filter(e -> userId.equals(e.getUserId()))
                .orElseThrow(() -> new ApplicationException("timeentry.not.found", null));

        TimeEntrySession session = sessionRepository.findById(sessionId)
                .filter(s -> entryId.equals(s.getEntryId()))
                .orElseThrow(() -> new ApplicationException("timeentry.session.not.found", null));

        session.setActive(false);
        session.setUpdatedBy(userId);
        sessionRepository.save(session);

        log.info("[Delete Session] Session {} of entry {} deactivated by user {}", sessionId, entryId, userId);

        return assembler.toDetail(entry);
    }
}
