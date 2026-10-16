package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectAssignmentFilterMapper;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntrySessionFilterMapper;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TagFilterMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.assembler.TimeEntryDetailAssembler;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.StartTimerInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntryDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.StartTimerUseCase;
import java.util.List;

import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.model.Tag;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TagFilter;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntrySessionFilter;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TagRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntrySessionRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectAssignmentFilter;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StartTimerUseCaseImpl implements StartTimerUseCase {

    private static final String FALLBACK_ENTRY_NAME = "Untitled";
    private static final Map<String, String> DEFAULT_ENTRY_NAMES = Map.of(
            "pt", "Sem título",
            "en", "Untitled");

    private final TimeEntryRepository timeEntryRepository;
    private final TimeEntrySessionRepository sessionRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final TagRepository tagRepository;
    private final TimeEntryDetailAssembler assembler;
    private final SecurityContextPort securityContext;

    public StartTimerUseCaseImpl(
            TimeEntryRepository timeEntryRepository,
            TimeEntrySessionRepository sessionRepository,
            ProjectRepository projectRepository,
            ProjectAssignmentRepository projectAssignmentRepository,
            TagRepository tagRepository,
            TimeEntryDetailAssembler assembler,
            SecurityContextPort securityContext) {
        this.timeEntryRepository = timeEntryRepository;
        this.sessionRepository = sessionRepository;
        this.projectRepository = projectRepository;
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.tagRepository = tagRepository;
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

        TimeEntrySessionFilter runningFilter = TimeEntrySessionFilterMapper.runningByUser(userId);
        if (sessionRepository.exists(runningFilter)) {
            throw new ApplicationException("timeentry.timer.already.running", null, HttpStatus.CONFLICT);
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
        UUID projectId = null;
        UUID companyId = null;

        if (input.projectId() != null) {
            Project project = projectRepository.findById(input.projectId())
                    .orElseThrow(() -> new ApplicationException("project.not.found", null, HttpStatus.NOT_FOUND));
            if (project.getStatus() != ProjectStatus.ACTIVE) {
                throw new ApplicationException("project.not.active", null, HttpStatus.CONFLICT);
            }
            if (project.getCompanyId() == null) {
                if (!userId.equals(project.getUserId())) {
                    throw new ApplicationException("timeentry.project.access.denied", null,
                            HttpStatus.FORBIDDEN);
                }
            } else if (!hasActiveAssignment(userId, project.getId())) {
                throw new ApplicationException("timeentry.project.access.denied", null, HttpStatus.FORBIDDEN);
            }
            projectId = project.getId();
            companyId = project.getCompanyId();
        }

        String name = input.name() == null || input.name().isBlank()
                ? resolveDefaultEntryName()
                : input.name().trim();

        List<UUID> tagIds = input.tagIds() == null ? List.of() : input.tagIds();
        TagFilter tagFilter = TagFilterMapper.activeByIdsAndUserId(tagIds, userId);
        List<Tag> tags = tagIds.isEmpty() ? List.of() : tagRepository.search(tagFilter);
        if (tags.size() != tagIds.size()) {
            throw new ApplicationException("timeentry.tag.not.found", null, HttpStatus.NOT_FOUND);
        }

        TimeEntry entry = new TimeEntry();
        entry.setUserId(userId);
        entry.setCompanyId(companyId);
        entry.setProjectId(projectId);
        entry.setName(name);
        entry.setDescription(input.description());
        entry.setActive(true);
        entry.setCreatedBy(userId);
        entry.setUpdatedBy(userId);

        TimeEntry saved = timeEntryRepository.save(entry);
        timeEntryRepository.replaceTags(saved.getId(), tagIds, userId);
        return saved;
    }

    private boolean hasActiveAssignment(UUID userId, UUID projectId) {
        ProjectAssignmentFilter filter = ProjectAssignmentFilterMapper.activeAssignment(userId, projectId);
        return projectAssignmentRepository.exists(filter);
    }

    private String resolveDefaultEntryName() {
        return securityContext.getCurrentUser()
                .map(user -> DEFAULT_ENTRY_NAMES.getOrDefault(
                        user.locale() != null ? user.locale().getLanguage() : "", FALLBACK_ENTRY_NAME))
                .orElse(FALLBACK_ENTRY_NAME);
    }
}
