package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.input.CreateProjectInputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.CreateProjectOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.CreateProjectUseCase;
import com.vmarcante.time_tracker.core.application.project.policy.ProjectValidationPolicy;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CreateProjectUseCaseImpl implements CreateProjectUseCase {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final ProjectValidationPolicy validationPolicy;
    private final SecurityContextPort securityContext;

    public CreateProjectUseCaseImpl(
            ProjectRepository projectRepository,
            CompanyRepository companyRepository,
            ProjectValidationPolicy validationPolicy,
            SecurityContextPort securityContext) {
        this.projectRepository = projectRepository;
        this.companyRepository = companyRepository;
        this.validationPolicy = validationPolicy;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public CreateProjectOutputDTO execute(UUID companyId, CreateProjectInputDTO input)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID actorId = currentUserId.get();

        CompanyMembership actorMembership = companyRepository.findMembership(actorId, companyId)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null));

        if (!actorMembership.getRole().canManage(CompanyRole.MANAGER)) {
            throw new ApplicationException("company.permission.denied", null);
        }

        validationPolicy.validateName(input.name());
        validationPolicy.validateDates(input.startDate(), input.endDate());

        if (projectRepository.existsActiveByCompanyIdAndName(companyId, input.name().trim())) {
            throw new ApplicationException("project.name.already.exists", null);
        }

        Project project = new Project();
        project.setCompanyId(companyId);
        project.setName(input.name().trim());
        project.setDescription(input.description());
        project.setStatus(ProjectStatus.ACTIVE);
        project.setStartDate(input.startDate());
        project.setEndDate(input.endDate());
        project.setActive(true);
        project.setCreatedBy(actorId);
        project.setUpdatedBy(actorId);

        Project saved = projectRepository.save(project);

        log.info("[Create Project] Project {} created in company {} by {}",
                saved.getId(), companyId, actorId);

        return CreateProjectOutputDTO.from(saved);
    }
}
