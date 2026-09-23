package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.RunningTimerOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.GetRunningTimerUseCase;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntrySessionRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class GetRunningTimerUseCaseImpl implements GetRunningTimerUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final TimeEntrySessionRepository sessionRepository;
    private final SecurityContextPort securityContext;

    public GetRunningTimerUseCaseImpl(
            TimeEntryRepository timeEntryRepository,
            TimeEntrySessionRepository sessionRepository,
            SecurityContextPort securityContext) {
        this.timeEntryRepository = timeEntryRepository;
        this.sessionRepository = sessionRepository;
        this.securityContext = securityContext;
    }

    @Override
    public RunningTimerOutputDTO execute() throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        Optional<TimeEntrySession> running = sessionRepository.findRunningByUserId(currentUserId.get());
        if (running.isEmpty()) {
            return null;
        }

        return timeEntryRepository.findById(running.get().getEntryId())
                .map(entry -> RunningTimerOutputDTO.from(entry, running.get()))
                .orElse(null);
    }
}
