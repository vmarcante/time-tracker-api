package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.orchestrator.ProjectTeamsSyncOrchestrator;
import com.vmarcante.time_tracker.core.application.project.dto.input.CreateProjectInputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.CreateProjectOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.CreateProjectUseCase;
import com.vmarcante.time_tracker.core.application.project.policy.ProjectValidationPolicy;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectFilter;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CreateProjectUseCaseImpl implements CreateProjectUseCase {

    private final ProjectRepository projectRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final ProjectValidationPolicy validationPolicy;
    private final SecurityContextPort securityContext;
    private final ProjectTeamsSyncOrchestrator projectTeamsSyncOrchestrator;

    public CreateProjectUseCaseImpl(
            ProjectRepository projectRepository,
            CompanyMembershipRepository membershipRepository,
            ProjectValidationPolicy validationPolicy,
            SecurityContextPort securityContext,
            ProjectTeamsSyncOrchestrator projectTeamsSyncOrchestrator) {
        this.projectRepository = projectRepository;
        this.membershipRepository = membershipRepository;
        this.validationPolicy = validationPolicy;
        this.securityContext = securityContext;
        this.projectTeamsSyncOrchestrator = projectTeamsSyncOrchestrator;
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

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(actorId, companyId);
        CompanyMembership actorMembership = membershipRepository.findOne(companyMembershipFilter)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN));

        if (!actorMembership.getRole().canManage(CompanyRole.MANAGER)) {
            throw new ApplicationException("company.permission.denied", null, HttpStatus.FORBIDDEN);
        }

        validationPolicy.validateName(input.name());
        validationPolicy.validateClientName(input.clientName());
        validationPolicy.validateDates(input.startDate(), input.endDate());

        ProjectFilter nameFilter = ProjectFilterMapper.activeByNameAndCompanyId(input.name().trim(), companyId);
        if (projectRepository.exists(nameFilter)) {
            throw new ApplicationException("project.name.already.exists", null, HttpStatus.CONFLICT);
        }

        Project project = new Project();
        project.setCompanyId(companyId);
        project.setName(input.name().trim());
        project.setClientName(input.clientName().trim());
        project.setDescription(input.description());
        project.setStatus(ProjectStatus.ACTIVE);
        project.setStartDate(input.startDate());
        project.setEndDate(input.endDate());
        project.setActive(true);
        project.setCreatedBy(actorId);
        project.setUpdatedBy(actorId);

        Project saved = projectRepository.save(project);

        if (input.teams() != null && !input.teams().isEmpty()) {
            projectTeamsSyncOrchestrator.execute(companyId, saved.getId(), input.teams(), actorId);
        }

        log.info("[Create Project] Project {} created in company {} by {}",
                saved.getId(), companyId, actorId);

        return CreateProjectOutputDTO.from(saved);
    }
}
