package com.vmarcante.time_tracker.core.application.project.link.use_cases;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.project.model.filter.TeamProjectFilterMapper;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.link.in.UnlinkProjectFromTeamUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.project.model.TeamProject;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.TeamProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectFilter;
import com.vmarcante.time_tracker.core.domain.project.model.filter.TeamProjectFilter;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilter;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UnlinkProjectFromTeamUseCaseImpl implements UnlinkProjectFromTeamUseCase {

    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final TeamProjectRepository teamProjectRepository;
    private final TeamRepository teamRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final SecurityContextPort securityContext;

    public UnlinkProjectFromTeamUseCaseImpl(
            ProjectRepository projectRepository,
            ProjectAssignmentRepository projectAssignmentRepository,
            TeamProjectRepository teamProjectRepository,
            TeamRepository teamRepository,
            CompanyMembershipRepository membershipRepository,
            SecurityContextPort securityContext) {
        this.projectRepository = projectRepository;
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.teamProjectRepository = teamProjectRepository;
        this.teamRepository = teamRepository;
        this.membershipRepository = membershipRepository;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public void execute(UUID companyId, UUID projectId, UUID teamId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID actorId = currentUserId.get();

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(actorId, companyId);
        CompanyMembership actorMembership = membershipRepository.findOne(companyMembershipFilter)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN));

        if (!actorMembership.getRole().canManage(CompanyRole.MANAGER)) {
            throw new ApplicationException("company.permission.denied", null, HttpStatus.FORBIDDEN);
        }

        ProjectFilter projectFilter = ProjectFilterMapper.activeByIdAndCompanyId(projectId, companyId);
        if (!projectRepository.exists(projectFilter)) {
            throw new ApplicationException("project.not.found", null, HttpStatus.NOT_FOUND);
        }

        TeamFilter teamFilter = TeamFilterMapper.activeByIdAndCompanyId(teamId, companyId);
        if (!teamRepository.exists(teamFilter)) {
            throw new ApplicationException("team.not.found", null, HttpStatus.NOT_FOUND);
        }

        TeamProjectFilter linkFilter = TeamProjectFilterMapper.activeLink(projectId, teamId);
        TeamProject link = teamProjectRepository.findOne(linkFilter)
                .orElseThrow(() -> new ApplicationException("project.not.linked", null, HttpStatus.CONFLICT));

        projectAssignmentRepository.deactivateAssignmentsByProjectIdAndTeamId(projectId, teamId, actorId);

        link.setActive(false);
        link.setUpdatedBy(actorId);
        teamProjectRepository.saveTeamLink(link);

        log.info("[Unlink Project from Team] Project {} unlinked from team {} by {} "
                + "(member assignments deactivated)", projectId, teamId, actorId);
    }
}
