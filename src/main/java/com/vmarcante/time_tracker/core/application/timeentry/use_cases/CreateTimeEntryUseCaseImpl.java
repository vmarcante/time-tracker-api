package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectAssignmentFilterMapper;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TagFilterMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.assembler.TimeEntryDetailAssembler;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.CreateTimeEntryInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntryDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.CreateTimeEntryUseCase;
import com.vmarcante.time_tracker.core.application.timeentry.policy.TimeEntryValidationPolicy;
import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.model.Tag;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TagFilter;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TagRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectAssignmentFilter;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CreateTimeEntryUseCaseImpl implements CreateTimeEntryUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final TagRepository tagRepository;
    private final TimeEntryValidationPolicy validationPolicy;
    private final TimeEntryDetailAssembler assembler;
    private final SecurityContextPort securityContext;

    public CreateTimeEntryUseCaseImpl(
            TimeEntryRepository timeEntryRepository,
            ProjectRepository projectRepository,
            ProjectAssignmentRepository projectAssignmentRepository,
            TagRepository tagRepository,
            TimeEntryValidationPolicy validationPolicy,
            TimeEntryDetailAssembler assembler,
            SecurityContextPort securityContext) {
        this.timeEntryRepository = timeEntryRepository;
        this.projectRepository = projectRepository;
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.tagRepository = tagRepository;
        this.validationPolicy = validationPolicy;
        this.assembler = assembler;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public TimeEntryDetailOutputDTO execute(CreateTimeEntryInputDTO input) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }
        UUID userId = currentUserId.get();

        validationPolicy.validateName(input.name());
        Project project = input.projectId() != null
                ? resolveProjectForLogging(userId, input.projectId())
                : null;

        List<UUID> tagIds = input.tagIds() == null ? List.of() : input.tagIds();
        TagFilter tagFilter = TagFilterMapper.activeByIdsAndUserId(tagIds, userId);
        List<Tag> tags = tagIds.isEmpty() ? List.of() : tagRepository.search(tagFilter);
        if (tags.size() != tagIds.size()) {
            throw new ApplicationException("timeentry.tag.not.found", null, HttpStatus.NOT_FOUND);
        }

        TimeEntry entry = new TimeEntry();
        entry.setUserId(userId);
        entry.setCompanyId(project != null ? project.getCompanyId() : null);
        entry.setProjectId(project != null ? project.getId() : null);
        entry.setName(input.name().trim());
        entry.setDescription(input.description());
        entry.setActive(true);
        entry.setCreatedBy(userId);
        entry.setUpdatedBy(userId);

        TimeEntry saved = timeEntryRepository.save(entry);
        timeEntryRepository.replaceTags(saved.getId(), tagIds, userId);

        log.info("[Create Time Entry] Entry {} created by user {} on project {}", saved.getId(), userId,
                project != null ? project.getId() : "none");

        return assembler.toDetail(saved);
    }

    private Project resolveProjectForLogging(UUID userId, UUID projectId) throws ApplicationException {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApplicationException("project.not.found", null, HttpStatus.NOT_FOUND));
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new ApplicationException("project.not.active", null, HttpStatus.CONFLICT);
        }
        if (project.getCompanyId() == null) {
            if (!userId.equals(project.getUserId())) {
                throw new ApplicationException("timeentry.project.access.denied", null, HttpStatus.FORBIDDEN);
            }
        } else if (!hasActiveAssignment(userId, projectId)) {
            throw new ApplicationException("timeentry.project.access.denied", null, HttpStatus.FORBIDDEN);
        }
        return project;
    }

    private boolean hasActiveAssignment(UUID userId, UUID projectId) {
        ProjectAssignmentFilter filter = ProjectAssignmentFilterMapper.activeAssignment(userId, projectId);
        return projectAssignmentRepository.exists(filter);
    }
}
