package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.in.DeactivatePersonalProjectUseCase;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DeactivatePersonalProjectUseCaseImpl implements DeactivatePersonalProjectUseCase {

    private final ProjectRepository projectRepository;
    private final SecurityContextPort securityContext;

    public DeactivatePersonalProjectUseCaseImpl(
            ProjectRepository projectRepository,
            SecurityContextPort securityContext) {
        this.projectRepository = projectRepository;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public void execute(UUID projectId) throws ApplicationException {
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

        project.setActive(false);
        project.setUpdatedBy(userId);
        projectRepository.save(project);

        log.info("[Deactivate Personal Project] Project {} deactivated by user {}", projectId, userId);
    }
}
