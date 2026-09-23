package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.FindPersonalProjectByIdUseCase;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class FindPersonalProjectByIdUseCaseImpl implements FindPersonalProjectByIdUseCase {

    private final ProjectRepository projectRepository;
    private final SecurityContextPort securityContext;

    public FindPersonalProjectByIdUseCaseImpl(
            ProjectRepository projectRepository,
            SecurityContextPort securityContext) {
        this.projectRepository = projectRepository;
        this.securityContext = securityContext;
    }

    @Override
    public ProjectDetailOutputDTO execute(UUID projectId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID userId = currentUserId.get();

        Project project = projectRepository.findById(projectId)
                .filter(p -> p.getCompanyId() == null)
                .filter(p -> userId.equals(p.getUserId()))
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new ApplicationException("project.not.found", null));

        return ProjectDetailOutputDTO.from(project, 0, 0);
    }
}
