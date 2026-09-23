package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.input.ProjectMemberEmailInputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectAssignmentOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.AssignProjectMemberUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;
import com.vmarcante.time_tracker.core.domain.project.event.ProjectAssignedEvent;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.model.ProjectAssignment;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AssignProjectMemberUseCaseImpl implements AssignProjectMemberUseCase {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final CompanyRepository companyRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;
    private final ApplicationEventPublisher eventPublisher;

    public AssignProjectMemberUseCaseImpl(
            ProjectRepository projectRepository,
            TeamRepository teamRepository,
            CompanyRepository companyRepository,
            PersonRepository personRepository,
            SecurityContextPort securityContext,
            ApplicationEventPublisher eventPublisher) {
        this.projectRepository = projectRepository;
        this.teamRepository = teamRepository;
        this.companyRepository = companyRepository;
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

        CompanyMembership actorMembership = companyRepository.findMembership(actorId, companyId)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null));

        if (!teamRepository.existsActiveByIdAndCompanyId(teamId, companyId)) {
            throw new ApplicationException("team.not.found", null);
        }

        boolean canManage = actorMembership.getRole().canManage(CompanyRole.MANAGER)
                || teamRepository.isTeamLead(actorId, teamId);
        if (!canManage) {
            throw new ApplicationException("project.permission.denied", null);
        }

        Project project = projectRepository.findById(projectId)
                .filter(p -> p.getCompanyId().equals(companyId))
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new ApplicationException("project.not.found", null));

        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new ApplicationException("project.not.active", null);
        }

        if (!projectRepository.isProjectLinkedToTeam(projectId, teamId)) {
            throw new ApplicationException("project.not.linked", null);
        }

        Person target = personRepository.findByEmail(input.email().address())
                .orElseThrow(() -> new ApplicationException("user.not.found", null));

        companyRepository.findMembership(target.getId(), companyId)
                .orElseThrow(() -> new ApplicationException("team.member.not.company.member", null));

        if (!teamRepository.isTeamMember(target.getId(), teamId)) {
            throw new ApplicationException("project.member.not.team.member", null);
        }

        if (projectRepository.hasActiveAssignment(target.getId(), projectId, teamId)) {
            throw new ApplicationException("project.assignment.exists", null);
        }

        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setUserId(target.getId());
        assignment.setProjectId(projectId);
        assignment.setTeamId(teamId);
        assignment.setActive(true);
        assignment.setCreatedBy(actorId);
        assignment.setUpdatedBy(actorId);

        ProjectAssignment saved = projectRepository.saveAssignment(assignment);

        eventPublisher.publishEvent(new ProjectAssignedEvent(
                target.getId(), projectId, teamId, companyId));

        log.info("[Assign Project Member] User {} assigned to project {} via team {} by {}",
                target.getId(), projectId, teamId, actorId);

        return ProjectAssignmentOutputDTO.from(saved, target.getName(), project.getName());
    }
}
