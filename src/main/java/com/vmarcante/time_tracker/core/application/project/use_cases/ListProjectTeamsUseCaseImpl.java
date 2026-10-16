package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.in.ListProjectTeamsUseCase;
import com.vmarcante.time_tracker.core.application.team.dto.input.TeamFilterInputDTO;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamSummaryOutputDTO;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.model.Team;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamMembershipRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;
import com.vmarcante.time_tracker.core.shared.utils.PageableUtils;

@Service
public class ListProjectTeamsUseCaseImpl implements ListProjectTeamsUseCase {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public ListProjectTeamsUseCaseImpl(
            ProjectRepository projectRepository,
            TeamRepository teamRepository,
            TeamMembershipRepository teamMembershipRepository,
            CompanyMembershipRepository membershipRepository,
            PersonRepository personRepository,
            SecurityContextPort securityContext) {
        this.projectRepository = projectRepository;
        this.teamRepository = teamRepository;
        this.teamMembershipRepository = teamMembershipRepository;
        this.membershipRepository = membershipRepository;
        this.personRepository = personRepository;
        this.securityContext = securityContext;
    }

    @Override
    public PageWrapperDTO<TeamSummaryOutputDTO> execute(UUID companyId, UUID projectId, TeamFilterInputDTO filter)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        if (!membershipRepository.isMember(currentUserId.get(), companyId)) {
            throw new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN);
        }

        if (!projectRepository.existsActiveByIdAndCompanyId(projectId, companyId)) {
            throw new ApplicationException("project.not.found", null, HttpStatus.NOT_FOUND);
        }

        Pageable pageable = PageableUtils.pageable(filter.getPageNumber(), filter.getPageSize(), filter.getSort());
        Page<Team> teams = teamRepository.findActiveByProjectId(projectId, pageable);

        if (teams.isEmpty()) {
            return PageWrapperDTO.of(teams.map(t -> TeamSummaryOutputDTO.from(t, null, 0)));
        }

        List<UUID> teamIds = teams.getContent().stream().map(t -> t.getId()).toList();

        Map<UUID, Long> memberCounts = teamMembershipRepository.countApprovedMembersByTeamIds(teamIds);
        Map<UUID, UUID> leadUserIds = teamMembershipRepository.findLeadUserIdsByTeamIds(teamIds);
        Map<UUID, String> leadNames = personRepository.findNamesByIds(leadUserIds.values());

        return PageWrapperDTO.of(teams.map(team -> {
            UUID leadUserId = leadUserIds.get(team.getId());
            String leadName = leadUserId != null ? leadNames.get(leadUserId) : null;
            long memberCount = memberCounts.getOrDefault(team.getId(), 0L);
            return TeamSummaryOutputDTO.from(team, leadName, memberCount);
        }));
    }
}
