package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.assembler.TimeEntryDetailAssembler;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.StartTimerInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntryDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.StartTimerUseCase;
import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntrySessionRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StartTimerUseCaseImpl implements StartTimerUseCase {

    private static final String DEFAULT_ENTRY_NAME = "Untitled";

    private final TimeEntryRepository timeEntryRepository;
    private final TimeEntrySessionRepository sessionRepository;
    private final ProjectRepository projectRepository;
    private final TimeEntryDetailAssembler assembler;
    private final SecurityContextPort securityContext;

    public StartTimerUseCaseImpl(
            TimeEntryRepository timeEntryRepository,
            TimeEntrySessionRepository sessionRepository,
            ProjectRepository projectRepository,
            TimeEntryDetailAssembler assembler,
            SecurityContextPort securityContext) {
        this.timeEntryRepository = timeEntryRepository;
        this.sessionRepository = sessionRepository;
        this.projectRepository = projectRepository;
        this.assembler = assembler;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public TimeEntryDetailOutputDTO execute(StartTimerInputDTO input) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }
        UUID userId = currentUserId.get();

        if (sessionRepository.existsRunningByUserId(userId)) {
            throw new ApplicationException("timeentry.timer.already.running", null);
        }

        TimeEntry entry;
        if (input.entryId() != null) {
            entry = timeEntryRepository.findById(input.entryId())
                    .filter(e -> userId.equals(e.getUserId()))
                    .orElseThrow(() -> new ApplicationException("timeentry.not.found", null));
        } else {
            entry = createQuickEntry(userId, input);
        }

        TimeEntrySession session = new TimeEntrySession();
        session.setEntryId(entry.getId());
        session.setUserId(userId);
        session.setStartTime(LocalDateTime.now());
        session.setEndTime(null);
        session.setActive(true);
        session.setCreatedBy(userId);
        session.setUpdatedBy(userId);

        sessionRepository.save(session);

        log.info("[Start Timer] Timer started on entry {} by user {}", entry.getId(), userId);

        return assembler.toDetail(entry);
    }

    private TimeEntry createQuickEntry(UUID userId, StartTimerInputDTO input) throws ApplicationException {
        if (input.projectId() == null) {
            throw new ApplicationException("timeentry.project.required", null);
        }
        Project project = projectRepository.findById(input.projectId())
                .orElseThrow(() -> new ApplicationException("project.not.found", null));
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new ApplicationException("project.not.active", null);
        }
        if (project.getCompanyId() == null) {
            if (!userId.equals(project.getUserId())) {
                throw new ApplicationException("timeentry.project.access.denied", null);
            }
        } else if (!projectRepository.hasAnyActiveAssignment(userId, project.getId())) {
            throw new ApplicationException("timeentry.project.access.denied", null);
        }

        String name = input.name() == null || input.name().isBlank()
                ? DEFAULT_ENTRY_NAME
                : input.name().trim();

        TimeEntry entry = new TimeEntry();
        entry.setUserId(userId);
        entry.setCompanyId(project.getCompanyId());
        entry.setProjectId(project.getId());
        entry.setName(name);
        entry.setDescription(input.description());
        entry.setActive(true);
        entry.setCreatedBy(userId);
        entry.setUpdatedBy(userId);

        return timeEntryRepository.save(entry);
    }
}
