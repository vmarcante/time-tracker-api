package com.vmarcante.time_tracker.core.application.team.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamWithMembersOutputDTO;
import com.vmarcante.time_tracker.core.application.team.in.ListCompanyTeamsWithMembersUseCase;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.team.model.Team;
import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamMembershipRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListCompanyTeamsWithMembersUseCaseImpl implements ListCompanyTeamsWithMembersUseCase {

    private final TeamRepository teamRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public ListCompanyTeamsWithMembersUseCaseImpl(
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
    public List<TeamWithMembersOutputDTO> execute(UUID companyId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        if (!membershipRepository.isMember(currentUserId.get(), companyId)) {
            throw new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN);
        }

        Page<Team> teams = teamRepository.findActiveByCompanyId(companyId, Pageable.unpaged());
        if (teams.isEmpty()) {
            return List.of();
        }

        List<UUID> teamIds = teams.getContent().stream()
                .map(team -> team.getId())
                .filter(Objects::nonNull)
                .toList();
        List<TeamMembership> memberships = teamMembershipRepository.findApprovedMembershipsByTeamIds(teamIds);

        List<UUID> userIds = memberships.stream()
                .map(membership -> membership.getUserId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<UUID, String> memberNames = personRepository.findNamesByIds(userIds);

        Map<UUID, List<TeamMembership>> membersByTeam = memberships.stream()
                .filter(membership -> membership.getTeamId() != null)
                .collect(Collectors.groupingBy(membership -> membership.getTeamId()));

        return teams.getContent().stream()
                .map(team -> {
                    List<TeamMemberOutputDTO> members = membersByTeam
                            .getOrDefault(team.getId(), List.of())
                            .stream()
                            .map(m -> TeamMemberOutputDTO.from(m, memberNames.get(m.getUserId())))
                            .toList();
                    return TeamWithMembersOutputDTO.from(team, members);
                })
                .toList();
    }
}
