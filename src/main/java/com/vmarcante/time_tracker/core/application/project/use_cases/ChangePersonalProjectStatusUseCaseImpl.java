package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.ChangePersonalProjectStatusUseCase;
import com.vmarcante.time_tracker.core.application.project.policy.ProjectValidationPolicy;
import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChangePersonalProjectStatusUseCaseImpl implements ChangePersonalProjectStatusUseCase {

    private final ProjectRepository projectRepository;
    private final ProjectValidationPolicy validationPolicy;
    private final SecurityContextPort securityContext;

    public ChangePersonalProjectStatusUseCaseImpl(
            ProjectRepository projectRepository,
            ProjectValidationPolicy validationPolicy,
            SecurityContextPort securityContext) {
        this.projectRepository = projectRepository;
        this.validationPolicy = validationPolicy;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public ProjectDetailOutputDTO execute(UUID projectId, ProjectStatus targetStatus)
            throws ApplicationException {
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

        validationPolicy.validateStatusTransition(project.getStatus(), targetStatus);

        project.setStatus(targetStatus);
        project.setUpdatedBy(userId);
        Project saved = projectRepository.save(project);

        log.info("[Change Personal Project Status] Project {} -> {} by user {}",
                projectId, targetStatus, userId);

        return ProjectDetailOutputDTO.from(saved, 0, 0);
    }
}
