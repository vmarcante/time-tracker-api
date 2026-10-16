package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectAssignmentFilterMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.assembler.TimeEntryDetailAssembler;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.UpdateTimeEntryInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntryDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.UpdateTimeEntryUseCase;
import com.vmarcante.time_tracker.core.application.timeentry.policy.TimeEntryValidationPolicy;
import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectAssignmentFilter;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UpdateTimeEntryUseCaseImpl implements UpdateTimeEntryUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final TimeEntryValidationPolicy validationPolicy;
    private final TimeEntryDetailAssembler assembler;
    private final SecurityContextPort securityContext;
    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;

    public UpdateTimeEntryUseCaseImpl(
            TimeEntryRepository timeEntryRepository,
            TimeEntryValidationPolicy validationPolicy,
            TimeEntryDetailAssembler assembler,
            SecurityContextPort securityContext,
            ProjectRepository projectRepository,
            ProjectAssignmentRepository projectAssignmentRepository) {
        this.timeEntryRepository = timeEntryRepository;
        this.validationPolicy = validationPolicy;
        this.assembler = assembler;
        this.securityContext = securityContext;
        this.projectRepository = projectRepository;
        this.projectAssignmentRepository = projectAssignmentRepository;
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
        applyProjectChange(entry, userId, input);
        entry.setUpdatedBy(userId);

        TimeEntry saved = timeEntryRepository.save(entry);

        log.info("[Update Time Entry] Entry {} updated by user {}", saved.getId(), userId);

        return assembler.toDetail(saved);
    }

    private void applyProjectChange(TimeEntry entry, UUID userId, UpdateTimeEntryInputDTO input)
            throws ApplicationException {
        if (Boolean.TRUE.equals(input.clearProject())) {
            entry.setProjectId(null);
            entry.setCompanyId(null);
            return;
        }
        if (input.projectId() == null) {
            return;
        }
        Project project = resolveLoggableProject(userId, input.projectId());
        entry.setProjectId(project.getId());
        entry.setCompanyId(project.getCompanyId());
    }

    private Project resolveLoggableProject(UUID userId, UUID projectId) throws ApplicationException {
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
