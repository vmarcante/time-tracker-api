package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.model.Tag;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TagRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CreateTimeEntryUseCaseImpl implements CreateTimeEntryUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final ProjectRepository projectRepository;
    private final TagRepository tagRepository;
    private final TimeEntryValidationPolicy validationPolicy;
    private final TimeEntryDetailAssembler assembler;
    private final SecurityContextPort securityContext;

    public CreateTimeEntryUseCaseImpl(
            TimeEntryRepository timeEntryRepository,
            ProjectRepository projectRepository,
            TagRepository tagRepository,
            TimeEntryValidationPolicy validationPolicy,
            TimeEntryDetailAssembler assembler,
            SecurityContextPort securityContext) {
        this.timeEntryRepository = timeEntryRepository;
        this.projectRepository = projectRepository;
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
        Project project = resolveProjectForLogging(userId, input.projectId());

        List<UUID> tagIds = input.tagIds() == null ? List.of() : input.tagIds();
        List<Tag> tags = tagRepository.findActiveByIdsAndUserId(tagIds, userId);
        if (tags.size() != tagIds.size()) {
            throw new ApplicationException("timeentry.tag.not.found", null);
        }

        TimeEntry entry = new TimeEntry();
        entry.setUserId(userId);
        entry.setCompanyId(project.getCompanyId());
        entry.setProjectId(project.getId());
        entry.setName(input.name().trim());
        entry.setDescription(input.description());
        entry.setActive(true);
        entry.setCreatedBy(userId);
        entry.setUpdatedBy(userId);

        TimeEntry saved = timeEntryRepository.save(entry);
        timeEntryRepository.replaceTags(saved.getId(), tagIds, userId);

        log.info("[Create Time Entry] Entry {} created by user {} on project {}", saved.getId(), userId, project.getId());

        return assembler.toDetail(saved);
    }

    private Project resolveProjectForLogging(UUID userId, UUID projectId) throws ApplicationException {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApplicationException("project.not.found", null));
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new ApplicationException("project.not.active", null);
        }
        if (project.getCompanyId() == null) {
            if (!userId.equals(project.getUserId())) {
                throw new ApplicationException("timeentry.project.access.denied", null);
            }
        } else if (!projectRepository.hasAnyActiveAssignment(userId, projectId)) {
            throw new ApplicationException("timeentry.project.access.denied", null);
        }
        return project;
    }
}
