package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.in.ListProjectTeamsUseCase;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamSummaryOutputDTO;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.model.Team;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListProjectTeamsUseCaseImpl implements ListProjectTeamsUseCase {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final CompanyRepository companyRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public ListProjectTeamsUseCaseImpl(
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
    public List<TeamSummaryOutputDTO> execute(UUID companyId, UUID projectId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        if (!companyRepository.isMember(currentUserId.get(), companyId)) {
            throw new ApplicationException("company.access.denied", null);
        }

        if (!projectRepository.existsActiveByIdAndCompanyId(projectId, companyId)) {
            throw new ApplicationException("project.not.found", null);
        }

        List<UUID> teamIds = projectRepository.findActiveTeamIdsByProjectId(projectId);
        if (teamIds.isEmpty()) {
            return List.of();
        }

        List<Team> teams = teamRepository.findActiveByIds(teamIds);

        Map<UUID, Long> memberCounts = teamRepository.countApprovedMembersByTeamIds(teamIds);
        Map<UUID, UUID> leadUserIds = teamRepository.findLeadUserIdsByTeamIds(teamIds);
        Map<UUID, String> leadNames = personRepository.findNamesByIds(leadUserIds.values());

        return teams.stream()
                .map(team -> {
                    UUID leadUserId = leadUserIds.get(team.getId());
                    String leadName = leadUserId != null ? leadNames.get(leadUserId) : null;
                    long memberCount = memberCounts.getOrDefault(team.getId(), 0L);
                    return TeamSummaryOutputDTO.from(team, leadName, memberCount);
                })
                .toList();
    }
}
