package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectAssignmentFilterMapper;
import com.vmarcante.time_tracker.core.domain.project.model.filter.TeamProjectFilterMapper;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamMembershipFilter;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import com.vmarcante.time_tracker.base.domain.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.input.ProjectMemberEmailInputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectAssignmentOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.AssignProjectMemberUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;
import com.vmarcante.time_tracker.core.domain.project.event.ProjectAssignedEvent;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.model.ProjectAssignment;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectAssignmentFilter;
import com.vmarcante.time_tracker.core.domain.project.model.filter.TeamProjectFilter;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.TeamProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.enums.TeamRole;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilter;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamMembershipRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AssignProjectMemberUseCaseImpl implements AssignProjectMemberUseCase {

    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final TeamProjectRepository teamProjectRepository;
    private final TeamRepository teamRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;
    private final DomainEventPublisher eventPublisher;

    public AssignProjectMemberUseCaseImpl(
            ProjectRepository projectRepository,
            ProjectAssignmentRepository projectAssignmentRepository,
            TeamProjectRepository teamProjectRepository,
            TeamRepository teamRepository,
            TeamMembershipRepository teamMembershipRepository,
            CompanyMembershipRepository membershipRepository,
            PersonRepository personRepository,
            SecurityContextPort securityContext,
            DomainEventPublisher eventPublisher) {
        this.projectRepository = projectRepository;
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.teamProjectRepository = teamProjectRepository;
        this.teamRepository = teamRepository;
        this.teamMembershipRepository = teamMembershipRepository;
        this.membershipRepository = membershipRepository;
        this.personRepository = personRepository;
        this.securityContext = securityContext;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ProjectAssignmentOutputDTO execute(
            UUID companyId, UUID teamId, UUID projectId, ProjectMemberEmailInputDTO input)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID actorId = currentUserId.get();

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(actorId, companyId);
        CompanyMembership actorMembership = membershipRepository.findOne(companyMembershipFilter)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN));

        TeamFilter teamFilter = TeamFilterMapper.activeByIdAndCompanyId(teamId, companyId);
        if (!teamRepository.exists(teamFilter)) {
            throw new ApplicationException("team.not.found", null, HttpStatus.NOT_FOUND);
        }

        TeamMembershipFilter teamMembershipFilter = TeamMembershipFilterMapper.approvedMembershipWithRole(actorId, teamId, TeamRole.LEAD);
        boolean canManage = actorMembership.getRole().canManage(CompanyRole.MANAGER)
                || teamMembershipRepository.exists(teamMembershipFilter);
        if (!canManage) {
            throw new ApplicationException("project.permission.denied", null, HttpStatus.FORBIDDEN);
        }

        Project project = projectRepository.findById(projectId)
                .filter(p -> p.getCompanyId().equals(companyId))
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new ApplicationException("project.not.found", null, HttpStatus.NOT_FOUND));

        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new ApplicationException("project.not.active", null, HttpStatus.CONFLICT);
        }

        TeamProjectFilter linkFilter = TeamProjectFilterMapper.activeLink(projectId, teamId);
        if (!teamProjectRepository.exists(linkFilter)) {
            throw new ApplicationException("project.not.linked", null, HttpStatus.CONFLICT);
        }

        Person target = personRepository.findByEmail(input.email().address())
                .orElseThrow(() -> new ApplicationException("user.not.found", null, HttpStatus.NOT_FOUND));

        CompanyMembershipFilter targetMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(target.getId(), companyId);
        membershipRepository.findOne(targetMembershipFilter)
                .orElseThrow(() -> new ApplicationException("team.member.not.company.member", null));

        TeamMembershipFilter targetTeamMembershipFilter = TeamMembershipFilterMapper.approvedMembership(target.getId(), teamId);
        if (!teamMembershipRepository
                .exists(targetTeamMembershipFilter)) {
            throw new ApplicationException("project.member.not.team.member", null);
        }

        ProjectAssignmentFilter assignmentFilter = ProjectAssignmentFilterMapper.activeAssignment(target.getId(), projectId, teamId);
        if (projectAssignmentRepository.exists(assignmentFilter)) {
            throw new ApplicationException("project.assignment.exists", null, HttpStatus.CONFLICT);
        }

        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setUserId(target.getId());
        assignment.setProjectId(projectId);
        assignment.setTeamId(teamId);
        assignment.setActive(true);
        assignment.setCreatedBy(actorId);
        assignment.setUpdatedBy(actorId);

        ProjectAssignment saved = projectAssignmentRepository.saveAssignment(assignment);

        eventPublisher.publish(new ProjectAssignedEvent(
                target.getId(), projectId, teamId, companyId));

        log.info("[Assign Project Member] User {} assigned to project {} via team {} by {}",
                target.getId(), projectId, teamId, actorId);

        return ProjectAssignmentOutputDTO.from(saved, target.getName(), project.getName());
    }
}
