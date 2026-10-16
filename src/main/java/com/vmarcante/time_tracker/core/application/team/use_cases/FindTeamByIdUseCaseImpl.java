package com.vmarcante.time_tracker.core.application.team.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamMembershipFilter;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.team.in.FindTeamByIdUseCase;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.team.model.Team;
import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamMembershipRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class FindTeamByIdUseCaseImpl implements FindTeamByIdUseCase {

    private final TeamRepository teamRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public FindTeamByIdUseCaseImpl(
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
    public TeamDetailOutputDTO execute(UUID companyId, UUID teamId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(currentUserId.get(), companyId);
        if (!membershipRepository.exists(companyMembershipFilter)) {
            throw new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN);
        }

        Team team = teamRepository.findById(teamId)
                .filter(t -> t.getCompanyId().equals(companyId))
                .filter(t -> Boolean.TRUE.equals(t.getActive()))
                .orElseThrow(() -> new ApplicationException("team.not.found", null, HttpStatus.NOT_FOUND));

        TeamMembershipFilter teamMembershipFilter = TeamMembershipFilterMapper.leadByTeam(teamId);
        Optional<TeamMembership> lead = teamMembershipRepository
                .findOne(teamMembershipFilter);
        UUID leadUserId = lead.isPresent() ? lead.get().getUserId() : null;
        String leadName = leadUserId != null
                ? personRepository.findNameById(leadUserId).orElse(null)
                : null;

        Map<UUID, Long> counts = teamMembershipRepository.countApprovedMembersByTeamIds(List.of(teamId));
        long memberCount = counts.getOrDefault(teamId, 0L);

        return TeamDetailOutputDTO.from(team, leadUserId, leadName, memberCount);
    }
}
