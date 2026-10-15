package com.vmarcante.time_tracker.core.application.project.orchestrator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.input.ProjectTeamMembersInputDTO;
import com.vmarcante.time_tracker.core.domain.project.event.ProjectAssignedEvent;
import com.vmarcante.time_tracker.core.domain.project.model.ProjectAssignment;
import com.vmarcante.time_tracker.core.domain.project.model.TeamProject;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.TeamProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.model.Team;
import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamMembershipRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;

class ProjectTeamsSyncOrchestratorTest {

    private ProjectAssignmentRepository projectAssignmentRepository;
    private TeamRepository teamRepository;
    private TeamMembershipRepository teamMembershipRepository;
    private TeamProjectRepository teamProjectRepository;
    private ApplicationEventPublisher eventPublisher;
    private ProjectTeamsSyncOrchestrator orchestrator;

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final UUID TEAM_ID = UUID.randomUUID();
    private static final UUID MEMBER_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        projectAssignmentRepository = mock(ProjectAssignmentRepository.class);
        teamRepository = mock(TeamRepository.class);
        teamMembershipRepository = mock(TeamMembershipRepository.class);
        teamProjectRepository = mock(TeamProjectRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        orchestrator = new ProjectTeamsSyncOrchestrator(
                projectAssignmentRepository, teamRepository, teamMembershipRepository,
                teamProjectRepository, eventPublisher);
    }

    private Team activeTeam() {
        Team team = new Team();
        team.setId(TEAM_ID);
        team.setCompanyId(COMPANY_ID);
        team.setActive(true);
        return team;
    }

    private TeamMembership approvedMembership(UUID teamId, UUID userId) {
        TeamMembership membership = new TeamMembership();
        membership.setTeamId(teamId);
        membership.setUserId(userId);
        membership.setActive(true);
        return membership;
    }

    private TeamProject activeLink(UUID teamId) {
        TeamProject link = new TeamProject();
        link.setId(UUID.randomUUID());
        link.setProjectId(PROJECT_ID);
        link.setTeamId(teamId);
        link.setActive(true);
        return link;
    }

    private ProjectAssignment activeAssignment(UUID teamId, UUID userId) {
        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setId(UUID.randomUUID());
        assignment.setUserId(userId);
        assignment.setProjectId(PROJECT_ID);
        assignment.setTeamId(teamId);
        assignment.setActive(true);
        return assignment;
    }

    @Test
    void givenNewTeamWithMembers_whenExecuting_thenShouldLinkTeamAndAssignMembers() {
        // Arrange
        when(teamRepository.findAllByIds(any())).thenReturn(List.of(activeTeam()));
        when(teamMembershipRepository.findApprovedMembershipsByTeamIds(any()))
                .thenReturn(List.of(approvedMembership(TEAM_ID, MEMBER_ID)));
        when(teamProjectRepository.findActiveTeamLinksByProjectId(PROJECT_ID)).thenReturn(List.of());
        when(projectAssignmentRepository.findAllActiveAssignmentsByProjectId(PROJECT_ID))
                .thenReturn(List.of());

        List<ProjectTeamMembersInputDTO> input = List.of(
                new ProjectTeamMembersInputDTO(TEAM_ID, List.of(MEMBER_ID)));

        // Act
        orchestrator.execute(COMPANY_ID, PROJECT_ID, input, ACTOR_ID);

        // Assert
        verify(teamProjectRepository).saveAllTeamLinks(anyList());
        verify(projectAssignmentRepository).saveAllAssignments(anyList());
        verify(eventPublisher).publishEvent(any(ProjectAssignedEvent.class));
    }

    @Test
    void givenRemovedTeam_whenExecuting_thenShouldDeactivateLinkAndAssignments() {
        // Arrange
        when(teamProjectRepository.findActiveTeamLinksByProjectId(PROJECT_ID))
                .thenReturn(List.of(activeLink(TEAM_ID)));
        when(projectAssignmentRepository.findAllActiveAssignmentsByProjectId(PROJECT_ID))
                .thenReturn(List.of(activeAssignment(TEAM_ID, MEMBER_ID)));

        List<ProjectTeamMembersInputDTO> input = List.of();

        // Act
        orchestrator.execute(COMPANY_ID, PROJECT_ID, input, ACTOR_ID);

        // Assert
        verify(teamProjectRepository).deactivateTeamLinksByIds(any(), any());
        verify(projectAssignmentRepository).deactivateAssignmentsByIds(any(), any());
        verify(projectAssignmentRepository, never()).saveAllAssignments(anyList());
    }

    @Test
    void givenMemberRemovedFromKeptTeam_whenExecuting_thenShouldDeactivateAssignment() {
        // Arrange
        when(teamRepository.findAllByIds(any())).thenReturn(List.of(activeTeam()));
        when(teamProjectRepository.findActiveTeamLinksByProjectId(PROJECT_ID))
                .thenReturn(List.of(activeLink(TEAM_ID)));
        when(projectAssignmentRepository.findAllActiveAssignmentsByProjectId(PROJECT_ID))
                .thenReturn(List.of(activeAssignment(TEAM_ID, MEMBER_ID)));

        List<ProjectTeamMembersInputDTO> input = List.of(
                new ProjectTeamMembersInputDTO(TEAM_ID, List.of()));

        // Act
        orchestrator.execute(COMPANY_ID, PROJECT_ID, input, ACTOR_ID);

        // Assert
        verify(projectAssignmentRepository).deactivateAssignmentsByIds(any(), any());
        verify(teamProjectRepository, never()).deactivateTeamLinksByIds(any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void givenMemberAlreadyAssigned_whenExecuting_thenShouldNotDuplicateAssignment() {
        // Arrange
        when(teamRepository.findAllByIds(any())).thenReturn(List.of(activeTeam()));
        when(teamMembershipRepository.findApprovedMembershipsByTeamIds(any()))
                .thenReturn(List.of(approvedMembership(TEAM_ID, MEMBER_ID)));
        when(teamProjectRepository.findActiveTeamLinksByProjectId(PROJECT_ID))
                .thenReturn(List.of(activeLink(TEAM_ID)));
        when(projectAssignmentRepository.findAllActiveAssignmentsByProjectId(PROJECT_ID))
                .thenReturn(List.of(activeAssignment(TEAM_ID, MEMBER_ID)));

        List<ProjectTeamMembersInputDTO> input = List.of(
                new ProjectTeamMembersInputDTO(TEAM_ID, List.of(MEMBER_ID)));

        // Act
        orchestrator.execute(COMPANY_ID, PROJECT_ID, input, ACTOR_ID);

        // Assert
        verify(projectAssignmentRepository, never()).saveAllAssignments(anyList());
        verify(teamProjectRepository, never()).saveAllTeamLinks(anyList());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void givenMemberNotInTeam_whenExecuting_thenShouldThrow() {
        // Arrange
        when(teamRepository.findAllByIds(any())).thenReturn(List.of(activeTeam()));
        when(teamMembershipRepository.findApprovedMembershipsByTeamIds(any()))
                .thenReturn(List.of());

        List<ProjectTeamMembersInputDTO> input = List.of(
                new ProjectTeamMembersInputDTO(TEAM_ID, List.of(MEMBER_ID)));

        // Act & Assert
        assertThatThrownBy(() -> orchestrator.execute(COMPANY_ID, PROJECT_ID, input, ACTOR_ID))
                .isInstanceOf(ApplicationException.class)
                .extracting("exceptionKey")
                .isEqualTo("project.member.not.team.member");

        verify(teamProjectRepository, never()).saveAllTeamLinks(anyList());
        verify(projectAssignmentRepository, never()).saveAllAssignments(anyList());
    }

    @Test
    void givenTeamFromAnotherCompany_whenExecuting_thenShouldThrowNotFound() {
        // Arrange
        Team foreignTeam = activeTeam();
        foreignTeam.setCompanyId(UUID.randomUUID());
        when(teamRepository.findAllByIds(any())).thenReturn(List.of(foreignTeam));

        List<ProjectTeamMembersInputDTO> input = List.of(
                new ProjectTeamMembersInputDTO(TEAM_ID, List.of()));

        // Act & Assert
        assertThatThrownBy(() -> orchestrator.execute(COMPANY_ID, PROJECT_ID, input, ACTOR_ID))
                .isInstanceOf(ApplicationException.class)
                .extracting("exceptionKey")
                .isEqualTo("team.not.found");

        verify(teamProjectRepository, never()).saveAllTeamLinks(anyList());
        verify(projectAssignmentRepository, never()).saveAllAssignments(anyList());
    }

    @Test
    void givenDuplicateTeamEntries_whenExecuting_thenShouldMergeMemberLists() {
        // Arrange
        when(teamRepository.findAllByIds(any())).thenReturn(List.of(activeTeam()));
        when(teamMembershipRepository.findApprovedMembershipsByTeamIds(any()))
                .thenReturn(List.of(approvedMembership(TEAM_ID, MEMBER_ID)));
        when(teamProjectRepository.findActiveTeamLinksByProjectId(PROJECT_ID))
                .thenReturn(List.of(activeLink(TEAM_ID)));
        when(projectAssignmentRepository.findAllActiveAssignmentsByProjectId(PROJECT_ID))
                .thenReturn(List.of());

        List<ProjectTeamMembersInputDTO> input = List.of(
                new ProjectTeamMembersInputDTO(TEAM_ID, List.of(MEMBER_ID)),
                new ProjectTeamMembersInputDTO(TEAM_ID, List.of()));

        // Act & Assert — member lists merge, link untouched
        assertThatCode(() -> orchestrator.execute(COMPANY_ID, PROJECT_ID, input, ACTOR_ID))
                .doesNotThrowAnyException();

        verify(teamProjectRepository, never()).saveAllTeamLinks(anyList());
        verify(projectAssignmentRepository).saveAllAssignments(anyList());
    }
}
