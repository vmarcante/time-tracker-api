package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.in.DeactivateTimeEntryUseCase;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntrySessionRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DeactivateTimeEntryUseCaseImpl implements DeactivateTimeEntryUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final TimeEntrySessionRepository sessionRepository;
    private final SecurityContextPort securityContext;

    public DeactivateTimeEntryUseCaseImpl(
            TimeEntryRepository timeEntryRepository,
            TimeEntrySessionRepository sessionRepository,
            SecurityContextPort securityContext) {
        this.timeEntryRepository = timeEntryRepository;
        this.sessionRepository = sessionRepository;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public void execute(UUID entryId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }
        UUID userId = currentUserId.get();

        TimeEntry entry = timeEntryRepository.findById(entryId)
                .filter(e -> userId.equals(e.getUserId()))
                .orElseThrow(() -> new ApplicationException("timeentry.not.found", null));

        for (TimeEntrySession session : sessionRepository.findActiveByEntryId(entryId)) {
            if (session.getEndTime() == null) {
                throw new ApplicationException("timeentry.running.cannot.deactivate", null);
            }
            session.setActive(false);
            session.setUpdatedBy(userId);
            sessionRepository.save(session);
        }

        entry.setActive(false);
        entry.setUpdatedBy(userId);
        timeEntryRepository.save(entry);

        log.info("[Deactivate Time Entry] Entry {} deactivated by user {}", entryId, userId);
    }
}
