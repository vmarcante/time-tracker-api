package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.input.UpdateProjectInputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.UpdatePersonalProjectUseCase;
import com.vmarcante.time_tracker.core.application.project.policy.ProjectValidationPolicy;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UpdatePersonalProjectUseCaseImpl implements UpdatePersonalProjectUseCase {

    private final ProjectRepository projectRepository;
    private final ProjectValidationPolicy validationPolicy;
    private final SecurityContextPort securityContext;

    public UpdatePersonalProjectUseCaseImpl(
            ProjectRepository projectRepository,
            ProjectValidationPolicy validationPolicy,
            SecurityContextPort securityContext) {
        this.projectRepository = projectRepository;
        this.validationPolicy = validationPolicy;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public ProjectDetailOutputDTO execute(UUID projectId, UpdateProjectInputDTO input)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID userId = currentUserId.get();

        Project project = findOwnedProject(projectId, userId);

        if (input.name() != null) {
            validationPolicy.validateName(input.name());
            String newName = input.name().trim();
            if (!newName.equalsIgnoreCase(project.getName())
                    && projectRepository.existsActiveByUserIdAndName(userId, newName)) {
                throw new ApplicationException("project.name.already.exists", null);
            }
            project.setName(newName);
        }

        if (input.clientName() != null) {
            validationPolicy.validateClientName(input.clientName());
            project.setClientName(input.clientName().trim());
        }

        if (input.description() != null) {
            project.setDescription(input.description().isBlank() ? null : input.description());
        }

        if (input.startDate() != null) {
            project.setStartDate(input.startDate());
        }

        if (input.endDate() != null) {
            project.setEndDate(input.endDate());
        }

        validationPolicy.validateDates(project.getStartDate(), project.getEndDate());

        project.setUpdatedBy(userId);
        Project saved = projectRepository.save(project);

        log.info("[Update Personal Project] Project {} updated by user {}", projectId, userId);

        return ProjectDetailOutputDTO.from(saved, 0, 0);
    }

    private Project findOwnedProject(UUID projectId, UUID userId) throws ApplicationException {
        return projectRepository.findById(projectId)
                .filter(p -> p.getCompanyId() == null)
                .filter(p -> userId.equals(p.getUserId()))
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new ApplicationException("project.not.found", null));
    }
}
