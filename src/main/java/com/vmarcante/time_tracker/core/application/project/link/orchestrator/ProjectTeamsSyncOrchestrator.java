package com.vmarcante.time_tracker.core.application.project.link.orchestrator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamMembershipFilter;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilterMapper;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectAssignmentFilterMapper;
import com.vmarcante.time_tracker.core.domain.project.model.filter.TeamProjectFilterMapper;

import com.vmarcante.time_tracker.base.domain.event.DomainEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.link.dto.input.ProjectTeamMembersInputDTO;
import com.vmarcante.time_tracker.core.domain.project.event.ProjectAssignedEvent;
import com.vmarcante.time_tracker.core.domain.project.model.ProjectAssignment;
import com.vmarcante.time_tracker.core.domain.project.model.TeamProject;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectAssignmentFilter;
import com.vmarcante.time_tracker.core.domain.project.model.filter.TeamProjectFilter;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.TeamProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilter;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamMembershipRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;

@Service
public class ProjectTeamsSyncOrchestrator {

    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final TeamRepository teamRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final TeamProjectRepository teamProjectRepository;
    private final DomainEventPublisher eventPublisher;

    public ProjectTeamsSyncOrchestrator(
            ProjectAssignmentRepository projectAssignmentRepository,
            TeamRepository teamRepository,
            TeamMembershipRepository teamMembershipRepository,
            TeamProjectRepository teamProjectRepository,
            DomainEventPublisher eventPublisher) {
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.teamRepository = teamRepository;
        this.teamMembershipRepository = teamMembershipRepository;
        this.teamProjectRepository = teamProjectRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Synchronizes the project-team links and member assignments to match the
     * input structure. Teams absent from the input are unlinked and their
     * assignments deactivated; members absent from each team entry are
     * unassigned.
     */
    public void execute(
            UUID companyId,
            UUID projectId,
            List<ProjectTeamMembersInputDTO> teams,
            UUID actorId) throws ApplicationException {

        Map<UUID, Set<UUID>> inputByTeam = new LinkedHashMap<>();
        for (ProjectTeamMembersInputDTO team : teams) {
            inputByTeam.computeIfAbsent(team.teamId(), k -> new HashSet<>())
                    .addAll(team.memberIds() == null ? List.of() : team.memberIds());
        }

        validateTeamsAndMembers(companyId, inputByTeam);

        TeamProjectFilter linkFilter = TeamProjectFilterMapper.activeByProjectId(projectId);
        List<TeamProject> currentLinks = teamProjectRepository.search(linkFilter);
        ProjectAssignmentFilter assignmentFilter = ProjectAssignmentFilterMapper.activeByProjectId(projectId);
        List<ProjectAssignment> currentAssignments = projectAssignmentRepository.search(assignmentFilter);

        Set<UUID> linkedTeamIds = currentLinks.stream()
                .map(link -> link.getTeamId())
                .collect(Collectors.toSet());
        Set<String> linkedPairKeys = currentAssignments.stream()
                .map(a -> assignmentKey(a.getTeamId(), a.getUserId()))
                .collect(Collectors.toSet());

        List<UUID> linkIdsToDeactivate = new ArrayList<>();
        List<UUID> assignmentIdsToDeactivate = new ArrayList<>();
        List<TeamProject> linksToCreate = new ArrayList<>();
        List<ProjectAssignment> assignmentsToCreate = new ArrayList<>();

        for (TeamProject link : currentLinks) {
            if (!inputByTeam.containsKey(link.getTeamId())) {
                linkIdsToDeactivate.add(link.getId());
            }
        }

        for (ProjectAssignment assignment : currentAssignments) {
            Set<UUID> inputMembers = inputByTeam.get(assignment.getTeamId());
            if (inputMembers == null || !inputMembers.contains(assignment.getUserId())) {
                assignmentIdsToDeactivate.add(assignment.getId());
            }
        }

        for (Map.Entry<UUID, Set<UUID>> entry : inputByTeam.entrySet()) {
            UUID teamId = entry.getKey();

            if (!linkedTeamIds.contains(teamId)) {
                TeamProject link = new TeamProject();
                link.setProjectId(projectId);
                link.setTeamId(teamId);
                link.setActive(true);
                link.setCreatedBy(actorId);
                link.setUpdatedBy(actorId);
                linksToCreate.add(link);
            }

            for (UUID memberId : entry.getValue()) {
                if (linkedPairKeys.contains(assignmentKey(teamId, memberId))) {
                    continue;
                }
                ProjectAssignment assignment = new ProjectAssignment();
                assignment.setUserId(memberId);
                assignment.setProjectId(projectId);
                assignment.setTeamId(teamId);
                assignment.setActive(true);
                assignment.setCreatedBy(actorId);
                assignment.setUpdatedBy(actorId);
                assignmentsToCreate.add(assignment);
            }
        }

        if (!linkIdsToDeactivate.isEmpty()) {
            teamProjectRepository.deactivateTeamLinksByIds(linkIdsToDeactivate, actorId);
        }
        if (!assignmentIdsToDeactivate.isEmpty()) {
            projectAssignmentRepository.deactivateAssignmentsByIds(assignmentIdsToDeactivate, actorId);
        }
        if (!linksToCreate.isEmpty()) {
            teamProjectRepository.saveAllTeamLinks(linksToCreate);
        }
        if (!assignmentsToCreate.isEmpty()) {
            projectAssignmentRepository.saveAllAssignments(assignmentsToCreate);
        }

        for (ProjectAssignment assignment : assignmentsToCreate) {
            eventPublisher.publish(new ProjectAssignedEvent(
                    assignment.getUserId(), projectId, assignment.getTeamId(), companyId));
        }
    }

    private void validateTeamsAndMembers(UUID companyId, Map<UUID, Set<UUID>> inputByTeam)
            throws ApplicationException {
        if (inputByTeam.isEmpty()) {
            return;
        }

        Set<UUID> teamIds = inputByTeam.keySet();

        TeamFilter teamFilter = TeamFilterMapper.activeByIds(teamIds);
        Map<UUID, UUID> teamCompanyById = teamRepository.search(teamFilter).stream()
                .collect(Collectors.toMap(t -> t.getId(), t -> t.getCompanyId()));

        for (UUID teamId : teamIds) {
            if (!companyId.equals(teamCompanyById.get(teamId))) {
                throw new ApplicationException("team.not.found", null, HttpStatus.NOT_FOUND);
            }
        }

        TeamMembershipFilter teamMembershipFilter = TeamMembershipFilterMapper.approvedByTeamIds(teamIds);
        Set<String> memberKeys = teamMembershipRepository
                .search(teamMembershipFilter).stream()
                .map(m -> assignmentKey(m.getTeamId(), m.getUserId()))
                .collect(Collectors.toSet());

        for (Map.Entry<UUID, Set<UUID>> entry : inputByTeam.entrySet()) {
            for (UUID memberId : entry.getValue()) {
                if (!memberKeys.contains(assignmentKey(entry.getKey(), memberId))) {
                    throw new ApplicationException("project.member.not.team.member", null);
                }
            }
        }
    }

    private static String assignmentKey(UUID teamId, UUID userId) {
        return teamId + ":" + userId;
    }
}
