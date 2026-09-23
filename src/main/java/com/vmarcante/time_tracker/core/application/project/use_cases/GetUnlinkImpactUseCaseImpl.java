package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectAssignmentOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.GetUnlinkImpactUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.model.ProjectAssignment;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class GetUnlinkImpactUseCaseImpl implements GetUnlinkImpactUseCase {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final CompanyRepository companyRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public GetUnlinkImpactUseCaseImpl(
            ProjectRepository projectRepository,
            TeamRepository teamRepository,
            CompanyRepository companyRepository,
            PersonRepository personRepository,
            SecurityContextPort securityContext) {
        this.projectRepository = projectRepository;
        this.teamRepository = teamRepository;
        this.companyRepository = companyRepository;
        this.personRepository = personRepository;
        this.securityContext = securityContext;
    }

    @Override
    public List<ProjectAssignmentOutputDTO> execute(UUID companyId, UUID projectId, UUID teamId)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        CompanyMembership actorMembership = companyRepository
                .findMembership(currentUserId.get(), companyId)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null));

        if (!actorMembership.getRole().canManage(CompanyRole.MANAGER)) {
            throw new ApplicationException("company.permission.denied", null);
        }

        Project project = projectRepository.findById(projectId)
                .filter(p -> p.getCompanyId().equals(companyId))
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new ApplicationException("project.not.found", null));

        if (!teamRepository.existsActiveByIdAndCompanyId(teamId, companyId)) {
            throw new ApplicationException("team.not.found", null);
        }

        List<ProjectAssignment> assignments = projectRepository
                .findActiveAssignmentsByProjectIdAndTeamId(projectId, teamId);

        if (assignments.isEmpty()) {
            return List.of();
        }

        List<UUID> userIds = assignments.stream()
                .map(a -> a.getUserId())
                .distinct()
                .toList();
        Map<UUID, String> userNames = personRepository.findNamesByIds(userIds);

        return assignments.stream()
                .map(a -> ProjectAssignmentOutputDTO.from(
                        a, userNames.get(a.getUserId()), project.getName()))
                .toList();
    }
}
