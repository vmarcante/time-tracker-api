package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.CompleteProjectUseCase;
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
public class CompleteProjectUseCaseImpl implements CompleteProjectUseCase {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final ProjectValidationPolicy validationPolicy;
    private final SecurityContextPort securityContext;

    public CompleteProjectUseCaseImpl(
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
    public ProjectDetailOutputDTO execute(UUID companyId, UUID projectId) throws ApplicationException {
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

        Project project = projectRepository.findById(projectId)
                .filter(p -> p.getCompanyId().equals(companyId))
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new ApplicationException("project.not.found", null));

        validationPolicy.validateStatusTransition(project.getStatus(), ProjectStatus.COMPLETED);

        project.setStatus(ProjectStatus.COMPLETED);
        project.setUpdatedBy(actorId);
        Project saved = projectRepository.save(project);

        log.info("[Complete Project] Project {} completed by {}", projectId, actorId);

        Map<UUID, Long> teamCounts = projectRepository
                .countActiveTeamsByProjectIds(List.of(projectId));
        Map<UUID, Long> memberCounts = projectRepository
                .countActiveAssignmentsByProjectIds(List.of(projectId));

        return ProjectDetailOutputDTO.from(
                saved,
                teamCounts.getOrDefault(projectId, 0L),
                memberCounts.getOrDefault(projectId, 0L));
    }
}
