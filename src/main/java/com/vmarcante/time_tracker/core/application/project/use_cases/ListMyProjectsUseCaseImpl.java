package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.base.application.pagination.PageableUtils;
import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.input.ProjectFilterInputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectSummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.ListMyProjectsUseCase;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.TeamProjectRepository;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectFilter;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListMyProjectsUseCaseImpl implements ListMyProjectsUseCase {

    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final TeamProjectRepository teamProjectRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final SecurityContextPort securityContext;

    public ListMyProjectsUseCaseImpl(
            ProjectRepository projectRepository,
            ProjectAssignmentRepository projectAssignmentRepository,
            TeamProjectRepository teamProjectRepository,
            CompanyMembershipRepository membershipRepository,
            SecurityContextPort securityContext) {
        this.projectRepository = projectRepository;
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.teamProjectRepository = teamProjectRepository;
        this.membershipRepository = membershipRepository;
        this.securityContext = securityContext;
    }

    @Override
    public PageWrapperDTO<ProjectSummaryOutputDTO> execute(UUID companyId, ProjectFilterInputDTO filter)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID userId = currentUserId.get();

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(userId, companyId);
        if (!membershipRepository.exists(companyMembershipFilter)) {
            throw new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN);
        }

        Pageable pageable = PageableUtils.pageable(filter.getPageNumber(), filter.getPageSize(), filter.getSort());
        ProjectFilter projectFilter = ProjectFilterMapper.activeAssignedToUserInCompany(companyId, userId);
        Page<Project> projects = projectRepository.search(projectFilter, pageable);

        if (projects.isEmpty()) {
            return PageWrapperDTO.of(projects.map(p -> ProjectSummaryOutputDTO.from(p, 0, 0)));
        }

        List<UUID> projectIds = projects.getContent().stream().map(p -> p.getId()).toList();

        Map<UUID, Long> teamCounts = teamProjectRepository.countActiveTeamsByProjectIds(projectIds);
        Map<UUID, Long> memberCounts = projectAssignmentRepository.countActiveAssignmentsByProjectIds(projectIds);

        return PageWrapperDTO.of(projects.map(project -> ProjectSummaryOutputDTO.from(
                project,
                teamCounts.getOrDefault(project.getId(), 0L),
                memberCounts.getOrDefault(project.getId(), 0L))));
    }
}
