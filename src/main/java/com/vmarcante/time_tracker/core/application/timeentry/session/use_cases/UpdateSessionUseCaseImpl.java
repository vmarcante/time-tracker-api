package com.vmarcante.time_tracker.core.application.timeentry.session.use_cases;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.entry.assembler.TimeEntryDetailAssembler;
import com.vmarcante.time_tracker.core.application.timeentry.session.dto.input.SessionInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.TimeEntryDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.session.policy.TimeEntrySessionValidationPolicy;
import com.vmarcante.time_tracker.core.application.timeentry.session.in.UpdateSessionUseCase;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntrySessionFilter;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntrySessionFilterMapper;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntrySessionRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UpdateSessionUseCaseImpl implements UpdateSessionUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final TimeEntrySessionRepository sessionRepository;
    private final TimeEntrySessionValidationPolicy validationPolicy;
    private final TimeEntryDetailAssembler assembler;
    private final SecurityContextPort securityContext;

    public UpdateSessionUseCaseImpl(
            TimeEntryRepository timeEntryRepository,
            TimeEntrySessionRepository sessionRepository,
            TimeEntrySessionValidationPolicy validationPolicy,
            TimeEntryDetailAssembler assembler,
            SecurityContextPort securityContext) {
        this.timeEntryRepository = timeEntryRepository;
        this.sessionRepository = sessionRepository;
        this.validationPolicy = validationPolicy;
        this.assembler = assembler;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public TimeEntryDetailOutputDTO execute(UUID entryId, UUID sessionId, SessionInputDTO input)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }
        UUID userId = currentUserId.get();

        TimeEntry entry = timeEntryRepository.findById(entryId)
                .filter(e -> userId.equals(e.getUserId()))
                .orElseThrow(() -> new ApplicationException("timeentry.not.found", null));

        TimeEntrySessionFilter sessionFilter = TimeEntrySessionFilterMapper.activeById(sessionId);
        TimeEntrySession session = sessionRepository.findOne(sessionFilter)
                .filter(s -> entryId.equals(s.getEntryId()))
                .orElseThrow(() -> new ApplicationException("timeentry.session.not.found", null));

        validationPolicy.validateSessionTimes(input.startTime(), input.endTime());
        TimeEntrySessionFilter runningFilter = TimeEntrySessionFilterMapper.runningByUser(userId);
        if (input.endTime() == null && session.getEndTime() != null
                && sessionRepository.exists(runningFilter)) {
            throw new ApplicationException("timeentry.timer.already.running", null, HttpStatus.CONFLICT);
        }

        LocalDateTime effectiveEnd = input.endTime() != null ? input.endTime() : LocalDateTime.now();
        List<TimeEntrySession> overlapping = sessionRepository.search(
                TimeEntrySessionFilterMapper.overlappingRangeByUser(userId, input.startTime(), effectiveEnd));
        validationPolicy.validateMaxParallelSessions(
                overlapping, sessionId, input.startTime(), input.endTime());

        session.setStartTime(input.startTime());
        session.setEndTime(input.endTime());
        session.setDescription(input.description());
        session.setUpdatedBy(userId);

        sessionRepository.save(session);

        log.info("[Update Session] Session {} of entry {} updated by user {}", sessionId, entryId, userId);

        return assembler.toDetail(entry);
    }
}
