package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.input.CreateProjectInputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.CreateProjectOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.CreatePersonalProjectUseCase;
import com.vmarcante.time_tracker.core.application.project.policy.ProjectValidationPolicy;
import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CreatePersonalProjectUseCaseImpl implements CreatePersonalProjectUseCase {

    private final ProjectRepository projectRepository;
    private final ProjectValidationPolicy validationPolicy;
    private final SecurityContextPort securityContext;

    public CreatePersonalProjectUseCaseImpl(
            ProjectRepository projectRepository,
            ProjectValidationPolicy validationPolicy,
            SecurityContextPort securityContext) {
        this.projectRepository = projectRepository;
        this.validationPolicy = validationPolicy;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public CreateProjectOutputDTO execute(CreateProjectInputDTO input) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID userId = currentUserId.get();

        validationPolicy.validateName(input.name());
        validationPolicy.validateClientName(input.clientName());
        validationPolicy.validateDates(input.startDate(), input.endDate());

        if (projectRepository.existsActiveByUserIdAndName(userId, input.name().trim())) {
            throw new ApplicationException("project.name.already.exists", null);
        }

        Project project = new Project();
        project.setUserId(userId);
        project.setName(input.name().trim());
        project.setClientName(input.clientName().trim());
        project.setDescription(input.description());
        project.setStatus(ProjectStatus.ACTIVE);
        project.setStartDate(input.startDate());
        project.setEndDate(input.endDate());
        project.setActive(true);
        project.setCreatedBy(userId);
        project.setUpdatedBy(userId);

        Project saved = projectRepository.save(project);

        log.info("[Create Personal Project] Project {} created by user {}", saved.getId(), userId);

        return CreateProjectOutputDTO.from(saved);
    }
}
