package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectAssignmentOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.GetMemberRemovalImpactUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.project.model.ProjectAssignment;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class GetMemberRemovalImpactUseCaseImpl implements GetMemberRemovalImpactUseCase {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final CompanyRepository companyRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public GetMemberRemovalImpactUseCaseImpl(
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
    public List<ProjectAssignmentOutputDTO> execute(UUID companyId, UUID teamId, UUID membershipId)
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
            throw new ApplicationException("team.permission.denied", null);
        }

        TeamMembership membership = teamRepository.findMembershipById(membershipId)
                .filter(m -> m.getTeamId().equals(teamId))
                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                .orElseThrow(() -> new ApplicationException("team.membership.not.found", null));

        List<ProjectAssignment> assignments = projectRepository
                .findActiveAssignmentsByUserIdAndTeamId(membership.getUserId(), teamId);

        if (assignments.isEmpty()) {
            return List.of();
        }

        List<UUID> projectIds = assignments.stream()
                .map(a -> a.getProjectId())
                .distinct()
                .toList();
        Map<UUID, String> projectNames = projectRepository.findNamesByIds(projectIds);

        String memberName = personRepository.findNameById(membership.getUserId()).orElse(null);

        return assignments.stream()
                .map(a -> ProjectAssignmentOutputDTO.from(
                        a, memberName, projectNames.get(a.getProjectId())))
                .toList();
    }
}
