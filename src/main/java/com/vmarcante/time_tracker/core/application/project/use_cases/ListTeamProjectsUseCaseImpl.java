package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectSummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.ListTeamProjectsUseCase;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListTeamProjectsUseCaseImpl implements ListTeamProjectsUseCase {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final CompanyRepository companyRepository;
    private final SecurityContextPort securityContext;

    public ListTeamProjectsUseCaseImpl(
            ProjectRepository projectRepository,
            TeamRepository teamRepository,
            CompanyRepository companyRepository,
            SecurityContextPort securityContext) {
        this.projectRepository = projectRepository;
        this.teamRepository = teamRepository;
        this.companyRepository = companyRepository;
        this.securityContext = securityContext;
    }

    @Override
    public Page<ProjectSummaryOutputDTO> execute(UUID companyId, UUID teamId, Pageable pageable)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        if (!companyRepository.isMember(currentUserId.get(), companyId)) {
            throw new ApplicationException("company.access.denied", null);
        }

        if (!teamRepository.existsActiveByIdAndCompanyId(teamId, companyId)) {
            throw new ApplicationException("team.not.found", null);
        }

        Page<Project> projects = projectRepository.findActiveByTeamId(teamId, pageable);

        if (projects.isEmpty()) {
            return projects.map(p -> ProjectSummaryOutputDTO.from(p, 0, 0));
        }

        List<UUID> projectIds = projects.getContent().stream().map(p -> p.getId()).toList();

        Map<UUID, Long> teamCounts = projectRepository.countActiveTeamsByProjectIds(projectIds);
        Map<UUID, Long> memberCounts = projectRepository.countActiveAssignmentsByProjectIds(projectIds);

        return projects.map(project -> ProjectSummaryOutputDTO.from(
                project,
                teamCounts.getOrDefault(project.getId(), 0L),
                memberCounts.getOrDefault(project.getId(), 0L)));
    }
}
