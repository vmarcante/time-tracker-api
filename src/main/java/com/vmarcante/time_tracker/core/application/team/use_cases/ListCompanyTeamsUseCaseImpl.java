package com.vmarcante.time_tracker.core.application.team.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.base.application.pagination.PageableUtils;
import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.input.TeamFilterInputDTO;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamSummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.team.in.ListCompanyTeamsUseCase;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.team.model.Team;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilter;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamMembershipRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListCompanyTeamsUseCaseImpl implements ListCompanyTeamsUseCase {

    private final TeamRepository teamRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public ListCompanyTeamsUseCaseImpl(
            TeamRepository teamRepository,
            TeamMembershipRepository teamMembershipRepository,
            CompanyMembershipRepository membershipRepository,
            PersonRepository personRepository,
            SecurityContextPort securityContext) {
        this.teamRepository = teamRepository;
        this.teamMembershipRepository = teamMembershipRepository;
        this.membershipRepository = membershipRepository;
        this.personRepository = personRepository;
        this.securityContext = securityContext;
    }

    @Override
    public PageWrapperDTO<TeamSummaryOutputDTO> execute(UUID companyId, TeamFilterInputDTO filter)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(currentUserId.get(), companyId);
        if (!membershipRepository.exists(companyMembershipFilter)) {
            throw new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN);
        }

        Pageable pageable = PageableUtils.pageable(filter.getPageNumber(), filter.getPageSize(), filter.getSort());
        TeamFilter teamFilter = TeamFilterMapper.activeByCompanyId(companyId);
        Page<Team> teams = teamRepository.search(teamFilter, pageable);

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
