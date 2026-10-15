package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntryProjectOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.ListMyTimeEntryProjectsUseCase;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListMyTimeEntryProjectsUseCaseImpl implements ListMyTimeEntryProjectsUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final ProjectRepository projectRepository;
    private final SecurityContextPort securityContext;

    public ListMyTimeEntryProjectsUseCaseImpl(
            TimeEntryRepository timeEntryRepository,
            ProjectRepository projectRepository,
            SecurityContextPort securityContext) {
        this.timeEntryRepository = timeEntryRepository;
        this.projectRepository = projectRepository;
        this.securityContext = securityContext;
    }

    @Override
    public List<TimeEntryProjectOutputDTO> execute() throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        List<UUID> projectIds = timeEntryRepository.findDistinctProjectIdsByUserId(currentUserId.get());
        if (projectIds.isEmpty()) {
            return List.of();
        }

        return projectRepository.findAllByIds(projectIds).stream()
                .sorted(Comparator.comparing(Project::getName, String.CASE_INSENSITIVE_ORDER))
                .map(TimeEntryProjectOutputDTO::from)
                .toList();
    }
}
