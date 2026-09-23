package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.assembler.TimeEntryDetailAssembler;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntryDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.StopTimerUseCase;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntrySessionRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StopTimerUseCaseImpl implements StopTimerUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final TimeEntrySessionRepository sessionRepository;
    private final TimeEntryDetailAssembler assembler;
    private final SecurityContextPort securityContext;

    public StopTimerUseCaseImpl(
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
    public TimeEntryDetailOutputDTO execute() throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }
        UUID userId = currentUserId.get();

        TimeEntrySession session = sessionRepository.findRunningByUserId(userId)
                .orElseThrow(() -> new ApplicationException("timeentry.timer.not.running", null));

        session.setEndTime(LocalDateTime.now());
        session.setUpdatedBy(userId);
        sessionRepository.save(session);

        TimeEntry entry = timeEntryRepository.findById(session.getEntryId())
                .orElseThrow(() -> new ApplicationException("timeentry.not.found", null));

        log.info("[Stop Timer] Timer stopped on entry {} by user {}", entry.getId(), userId);

        return assembler.toDetail(entry);
    }
}
