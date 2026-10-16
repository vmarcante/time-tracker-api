package com.vmarcante.time_tracker.core.application.team.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamMembershipFilter;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.base.application.pagination.PageableUtils;
import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.company.dto.input.MembershipFilterInputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.team.in.ListTeamMembersUseCase;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilter;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamMembershipRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListTeamMembersUseCaseImpl implements ListTeamMembersUseCase {

    private final TeamRepository teamRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public ListTeamMembersUseCaseImpl(
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
    public PageWrapperDTO<TeamMemberOutputDTO> execute(UUID companyId, UUID teamId,
            MembershipFilterInputDTO filter) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(currentUserId.get(), companyId);
        if (!membershipRepository.exists(companyMembershipFilter)) {
            throw new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN);
        }

        TeamFilter teamFilter = TeamFilterMapper.activeByIdAndCompanyId(teamId, companyId);
        if (!teamRepository.exists(teamFilter)) {
            throw new ApplicationException("team.not.found", null, HttpStatus.NOT_FOUND);
        }

        Pageable pageable = PageableUtils.pageable(filter.getPageNumber(), filter.getPageSize(), filter.getSort());
        TeamMembershipFilter teamMembershipFilter = TeamMembershipFilterMapper.approvedByTeam(teamId);
        Page<TeamMembership> members = teamMembershipRepository
                .search(teamMembershipFilter, pageable);

        if (members.isEmpty()) {
            return PageWrapperDTO.of(members.map(m -> TeamMemberOutputDTO.from(m, null)));
        }

        List<UUID> userIds = members.getContent().stream()
                .map(m -> m.getUserId())
                .distinct()
                .toList();

        Map<UUID, String> memberNames = personRepository.findNamesByIds(userIds);

        return PageWrapperDTO.of(members.map(membership -> TeamMemberOutputDTO.from(
                membership,
                memberNames.get(membership.getUserId()))));
    }
}
