package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.FindProjectByIdUseCase;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class FindProjectByIdUseCaseImpl implements FindProjectByIdUseCase {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final SecurityContextPort securityContext;

    public FindProjectByIdUseCaseImpl(
            ProjectRepository projectRepository,
            CompanyRepository companyRepository,
            SecurityContextPort securityContext) {
        this.projectRepository = projectRepository;
        this.companyRepository = companyRepository;
        this.securityContext = securityContext;
    }

    @Override
    public ProjectDetailOutputDTO execute(UUID companyId, UUID projectId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        if (!companyRepository.isMember(currentUserId.get(), companyId)) {
            throw new ApplicationException("company.access.denied", null);
        }

        Project project = projectRepository.findById(projectId)
                .filter(p -> p.getCompanyId().equals(companyId))
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new ApplicationException("project.not.found", null));

        Map<UUID, Long> teamCounts = projectRepository
                .countActiveTeamsByProjectIds(List.of(projectId));
        Map<UUID, Long> memberCounts = projectRepository
                .countActiveAssignmentsByProjectIds(List.of(projectId));

        return ProjectDetailOutputDTO.from(
                project,
                teamCounts.getOrDefault(projectId, 0L),
                memberCounts.getOrDefault(projectId, 0L));
    }
}
