package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectSummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.ListMyProjectsUseCase;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListMyProjectsUseCaseImpl implements ListMyProjectsUseCase {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final SecurityContextPort securityContext;

    public ListMyProjectsUseCaseImpl(
            ProjectRepository projectRepository,
            CompanyRepository companyRepository,
            SecurityContextPort securityContext) {
        this.projectRepository = projectRepository;
        this.companyRepository = companyRepository;
        this.securityContext = securityContext;
    }

    @Override
    public List<ProjectSummaryOutputDTO> execute(UUID companyId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID userId = currentUserId.get();

        if (!companyRepository.isMember(userId, companyId)) {
            throw new ApplicationException("company.access.denied", null);
        }

        List<Project> projects = projectRepository.findActiveProjectsByUserId(userId, companyId);

        if (projects.isEmpty()) {
            return List.of();
        }

        List<UUID> projectIds = projects.stream().map(p -> p.getId()).toList();

        Map<UUID, Long> teamCounts = projectRepository.countActiveTeamsByProjectIds(projectIds);
        Map<UUID, Long> memberCounts = projectRepository.countActiveAssignmentsByProjectIds(projectIds);

        return projects.stream()
                .map(project -> ProjectSummaryOutputDTO.from(
                        project,
                        teamCounts.getOrDefault(project.getId(), 0L),
                        memberCounts.getOrDefault(project.getId(), 0L)))
                .toList();
    }
}
