package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.List;
import java.util.Map;
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
import com.vmarcante.time_tracker.core.application.project.dto.input.UpdateProjectInputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.UpdateProjectUseCase;
import com.vmarcante.time_tracker.core.application.project.policy.ProjectValidationPolicy;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.TeamProjectRepository;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectFilter;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UpdateProjectUseCaseImpl implements UpdateProjectUseCase {

    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final TeamProjectRepository teamProjectRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final ProjectValidationPolicy validationPolicy;
    private final SecurityContextPort securityContext;
    private final ProjectTeamsSyncOrchestrator projectTeamsSyncOrchestrator;

    public UpdateProjectUseCaseImpl(
            ProjectRepository projectRepository,
            ProjectAssignmentRepository projectAssignmentRepository,
            TeamProjectRepository teamProjectRepository,
            CompanyMembershipRepository membershipRepository,
            ProjectValidationPolicy validationPolicy,
            SecurityContextPort securityContext,
            ProjectTeamsSyncOrchestrator projectTeamsSyncOrchestrator) {
        this.projectRepository = projectRepository;
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.teamProjectRepository = teamProjectRepository;
        this.membershipRepository = membershipRepository;
        this.validationPolicy = validationPolicy;
        this.securityContext = securityContext;
        this.projectTeamsSyncOrchestrator = projectTeamsSyncOrchestrator;
    }

    @Override
    @Transactional
    public ProjectDetailOutputDTO execute(UUID companyId, UUID projectId, UpdateProjectInputDTO input)
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

        Project project = projectRepository.findById(projectId)
                .filter(p -> p.getCompanyId().equals(companyId))
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new ApplicationException("project.not.found", null, HttpStatus.NOT_FOUND));

        if (input.name() != null) {
            validationPolicy.validateName(input.name());
            String newName = input.name().trim();
            ProjectFilter nameFilter = ProjectFilterMapper.activeByNameAndCompanyId(newName, companyId);
            if (!newName.equalsIgnoreCase(project.getName())
                    && projectRepository.exists(nameFilter)) {
                throw new ApplicationException("project.name.already.exists", null, HttpStatus.CONFLICT);
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

        project.setUpdatedBy(actorId);
        Project saved = projectRepository.save(project);

        if (input.teams() != null) {
            projectTeamsSyncOrchestrator.execute(companyId, projectId, input.teams(), actorId);
        }

        log.info("[Update Project] Project {} updated by {}", projectId, actorId);

        Map<UUID, Long> teamCounts = teamProjectRepository
                .countActiveTeamsByProjectIds(List.of(projectId));
        Map<UUID, Long> memberCounts = projectAssignmentRepository
                .countActiveAssignmentsByProjectIds(List.of(projectId));

        return ProjectDetailOutputDTO.from(
                saved,
                teamCounts.getOrDefault(projectId, 0L),
                memberCounts.getOrDefault(projectId, 0L));
    }
}
