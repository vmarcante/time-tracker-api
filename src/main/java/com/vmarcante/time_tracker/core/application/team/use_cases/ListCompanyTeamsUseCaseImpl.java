package com.vmarcante.time_tracker.core.application.team.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamSummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.team.in.ListCompanyTeamsUseCase;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.team.model.Team;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListCompanyTeamsUseCaseImpl implements ListCompanyTeamsUseCase {

    private final TeamRepository teamRepository;
    private final CompanyRepository companyRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public ListCompanyTeamsUseCaseImpl(
            TeamRepository teamRepository,
            CompanyRepository companyRepository,
            PersonRepository personRepository,
            SecurityContextPort securityContext) {
        this.teamRepository = teamRepository;
        this.companyRepository = companyRepository;
        this.personRepository = personRepository;
        this.securityContext = securityContext;
    }

    @Override
    public Page<TeamSummaryOutputDTO> execute(UUID companyId, Pageable pageable)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        if (!companyRepository.isMember(currentUserId.get(), companyId)) {
            throw new ApplicationException("company.access.denied", null);
        }

        Page<Team> teams = teamRepository.findActiveByCompanyId(companyId, pageable);

        if (teams.isEmpty()) {
            return teams.map(t -> TeamSummaryOutputDTO.from(t, null, 0));
        }

        List<UUID> teamIds = teams.getContent().stream().map(t -> t.getId()).toList();

        Map<UUID, Long> memberCounts = teamRepository.countApprovedMembersByTeamIds(teamIds);
        Map<UUID, UUID> leadUserIds = teamRepository.findLeadUserIdsByTeamIds(teamIds);
        Map<UUID, String> leadNames = personRepository.findNamesByIds(leadUserIds.values());

        return teams.map(team -> {
            UUID leadUserId = leadUserIds.get(team.getId());
            String leadName = leadUserId != null ? leadNames.get(leadUserId) : null;
            long memberCount = memberCounts.getOrDefault(team.getId(), 0L);
            return TeamSummaryOutputDTO.from(team, leadName, memberCount);
        });
    }
}
