package com.vmarcante.time_tracker.core.application.company.orchestrator;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyOverviewOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.GetCompanyOverviewUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectSummaryOutputDTO;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.TeamProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class CompanyOverviewOrchestrator implements GetCompanyOverviewUseCase {

    private static final int RECENT_LIMIT = 5;

    private final CompanyMembershipRepository membershipRepository;
    private final TeamRepository teamRepository;
    private final ProjectRepository projectRepository;
    private final TeamProjectRepository teamProjectRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public CompanyOverviewOrchestrator(
            CompanyMembershipRepository membershipRepository,
            TeamRepository teamRepository,
            ProjectRepository projectRepository,
            TeamProjectRepository teamProjectRepository,
            ProjectAssignmentRepository projectAssignmentRepository,
            PersonRepository personRepository,
            SecurityContextPort securityContext) {
        this.membershipRepository = membershipRepository;
        this.teamRepository = teamRepository;
        this.projectRepository = projectRepository;
        this.teamProjectRepository = teamProjectRepository;
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.personRepository = personRepository;
        this.securityContext = securityContext;
    }

    @Override
    public CompanyOverviewOutputDTO execute(UUID companyId) throws ApplicationException {
        assertManagerRole(companyId);

        Pageable recentPageable = PageRequest.of(0, RECENT_LIMIT, Sort.by("createdAt").descending());

        Page<CompanyMembership> membersPage =
                membershipRepository.findApprovedMembershipsByCompanyId(companyId, recentPageable);
        Page<CompanyMembership> pendingPage =
                membershipRepository.findPendingMembershipsByCompanyId(companyId, recentPageable);
        long teamsCount = teamRepository
                .findActiveByCompanyId(companyId, PageRequest.of(0, 1))
                .getTotalElements();
        Page<Project> projectsPage =
                projectRepository.findActiveByCompanyId(companyId, recentPageable);

        Map<UUID, String> personNames = resolveNames(membersPage.getContent(), pendingPage.getContent());

        List<CompanyMemberOutputDTO> recentMembers = membersPage.getContent().stream()
                .map(membership -> CompanyMemberOutputDTO.from(
                        membership, personNames.get(membership.getUserId())))
                .toList();
        List<CompanyMemberOutputDTO> pendingRequests = pendingPage.getContent().stream()
                .map(membership -> CompanyMemberOutputDTO.from(
                        membership, personNames.get(membership.getUserId())))
                .toList();
        List<ProjectSummaryOutputDTO> recentProjects = mapRecentProjects(projectsPage.getContent());

        return new CompanyOverviewOutputDTO(
                membersPage.getTotalElements(),
                pendingPage.getTotalElements(),
                teamsCount,
                projectsPage.getTotalElements(),
                recentMembers,
                pendingRequests,
                recentProjects);
    }

    private Map<UUID, String> resolveNames(
            List<CompanyMembership> members, List<CompanyMembership> pending) {
        List<UUID> userIds = Stream.concat(members.stream(), pending.stream())
                .map(membership -> membership.getUserId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return personRepository.findNamesByIds(userIds);
    }

    private List<ProjectSummaryOutputDTO> mapRecentProjects(List<Project> projects) {
        List<UUID> projectIds = projects.stream()
                .map(project -> project.getId())
                .filter(Objects::nonNull)
                .toList();
        if (projectIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, Long> teamCounts = teamProjectRepository.countActiveTeamsByProjectIds(projectIds);
        Map<UUID, Long> memberCounts = projectAssignmentRepository.countActiveAssignmentsByProjectIds(projectIds);

        return projects.stream()
                .map(project -> ProjectSummaryOutputDTO.from(
                        project,
                        teamCounts.getOrDefault(project.getId(), 0L),
                        memberCounts.getOrDefault(project.getId(), 0L)))
                .toList();
    }

    private void assertManagerRole(UUID companyId) throws ApplicationException {
        UUID userId = securityContext.getCurrentUserId()
                .orElseThrow(() -> new ApplicationException("user.authenticated.not", null));

        CompanyMembership actorMembership = membershipRepository.findMembership(userId, companyId)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN));

        if (!actorMembership.getRole().canManage(CompanyRole.MEMBER)) {
            throw new ApplicationException("company.permission.denied", null, HttpStatus.FORBIDDEN);
        }
    }
}
