package com.vmarcante.time_tracker.core.application.company.overview.orchestrator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.application.company.overview.dto.output.CompanyOverviewOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectFilter;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.TeamProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.model.Team;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilter;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

class CompanyOverviewOrchestratorTest {

    private CompanyMembershipRepository membershipRepository;
    private TeamRepository teamRepository;
    private ProjectRepository projectRepository;
    private TeamProjectRepository teamProjectRepository;
    private ProjectAssignmentRepository projectAssignmentRepository;
    private PersonRepository personRepository;
    private SecurityContextPort securityContext;
    private CompanyOverviewOrchestrator orchestrator;

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID = UUID.randomUUID();
    private static final UUID MEMBER_ID = UUID.randomUUID();
    private static final UUID PENDING_ID = UUID.randomUUID();
    private static final UUID PROJECT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        membershipRepository = mock(CompanyMembershipRepository.class);
        teamRepository = mock(TeamRepository.class);
        projectRepository = mock(ProjectRepository.class);
        teamProjectRepository = mock(TeamProjectRepository.class);
        projectAssignmentRepository = mock(ProjectAssignmentRepository.class);
        personRepository = mock(PersonRepository.class);
        securityContext = mock(SecurityContextPort.class);
        orchestrator = new CompanyOverviewOrchestrator(
                membershipRepository, teamRepository, projectRepository,
                teamProjectRepository, projectAssignmentRepository,
                personRepository, securityContext);
    }

    private CompanyMembership membership(UUID userId, CompanyRole role, boolean approved) {
        CompanyMembership membership = new CompanyMembership();
        membership.setId(UUID.randomUUID());
        membership.setUserId(userId);
        membership.setCompanyId(COMPANY_ID);
        membership.setRole(role);
        membership.setApproved(approved);
        membership.setActive(true);
        return membership;
    }

    private void actorWithRole(CompanyRole role) {
        when(securityContext.getCurrentUserId()).thenReturn(Optional.of(ACTOR_ID));
        when(membershipRepository.findOne(any(CompanyMembershipFilter.class)))
                .thenReturn(Optional.of(membership(ACTOR_ID, role, true)));
    }

    private void emptyPages() {
        Page<CompanyMembership> emptyMemberships = Page.empty();
        Page<Team> emptyTeams = Page.empty();
        Page<Project> emptyProjects = Page.empty();

        when(membershipRepository.search(any(CompanyMembershipFilter.class), any(Pageable.class)))
                .thenReturn(emptyMemberships);
        when(teamRepository.search(any(TeamFilter.class), any(Pageable.class)))
                .thenReturn(emptyTeams);
        when(projectRepository.search(any(ProjectFilter.class), any(Pageable.class)))
                .thenReturn(emptyProjects);
    }

    @Test
    void shouldReturnCountsAndRecents_WhenActorCanManageMembers() throws ApplicationException {
        actorWithRole(CompanyRole.MANAGER);

        CompanyMembership member = membership(MEMBER_ID, CompanyRole.MEMBER, true);
        CompanyMembership pending = membership(PENDING_ID, CompanyRole.MEMBER, false);
        Project project = new Project();
        project.setId(PROJECT_ID);
        project.setCompanyId(COMPANY_ID);
        project.setActive(true);

        when(membershipRepository.search(
                argThat(f -> f != null && Boolean.TRUE.equals(f.getApproved())), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(member), Pageable.unpaged(), 12));
        when(membershipRepository.search(
                argThat(f -> f != null && Boolean.FALSE.equals(f.getApproved())), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(pending), Pageable.unpaged(), 3));
        when(teamRepository.search(any(TeamFilter.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), Pageable.unpaged(), 4));
        when(projectRepository.search(any(ProjectFilter.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(project), Pageable.unpaged(), 7));
        when(personRepository.findNamesByIds(anyList()))
                .thenReturn(Map.of(MEMBER_ID, "Alice", PENDING_ID, "Bob"));
        when(teamProjectRepository.countActiveTeamsByProjectIds(anyList()))
                .thenReturn(Map.of(PROJECT_ID, 2L));
        when(projectAssignmentRepository.countActiveAssignmentsByProjectIds(anyList()))
                .thenReturn(Map.of(PROJECT_ID, 5L));

        CompanyOverviewOutputDTO result = orchestrator.execute(COMPANY_ID);

        assertThat(result.membersCount()).isEqualTo(12);
        assertThat(result.pendingMembershipsCount()).isEqualTo(3);
        assertThat(result.teamsCount()).isEqualTo(4);
        assertThat(result.projectsCount()).isEqualTo(7);
        assertThat(result.recentMembers()).hasSize(1);
        assertThat(result.recentMembers().get(0).name()).isEqualTo("Alice");
        assertThat(result.pendingRequests()).hasSize(1);
        assertThat(result.pendingRequests().get(0).name()).isEqualTo("Bob");
        assertThat(result.recentProjects()).hasSize(1);
        assertThat(result.recentProjects().get(0).teamCount()).isEqualTo(2);
        assertThat(result.recentProjects().get(0).memberCount()).isEqualTo(5);
    }

    @Test
    void shouldReturnZerosAndEmptyLists_WhenCompanyHasNoData() throws ApplicationException {
        actorWithRole(CompanyRole.ADMIN);
        emptyPages();

        CompanyOverviewOutputDTO result = orchestrator.execute(COMPANY_ID);

        assertThat(result.membersCount()).isZero();
        assertThat(result.pendingMembershipsCount()).isZero();
        assertThat(result.teamsCount()).isZero();
        assertThat(result.projectsCount()).isZero();
        assertThat(result.recentMembers()).isEmpty();
        assertThat(result.pendingRequests()).isEmpty();
        assertThat(result.recentProjects()).isEmpty();
        verify(teamProjectRepository, never()).countActiveTeamsByProjectIds(anyList());
        verify(projectAssignmentRepository, never()).countActiveAssignmentsByProjectIds(anyList());
    }

    @Test
    void shouldThrowForbidden_WhenActorIsMember() {
        actorWithRole(CompanyRole.MEMBER);

        assertThatThrownBy(() -> orchestrator.execute(COMPANY_ID))
                .isInstanceOf(ApplicationException.class);
    }
}
