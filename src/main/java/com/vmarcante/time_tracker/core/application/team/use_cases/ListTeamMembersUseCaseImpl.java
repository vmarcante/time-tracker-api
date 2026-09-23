package com.vmarcante.time_tracker.core.application.team.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.team.in.ListTeamMembersUseCase;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListTeamMembersUseCaseImpl implements ListTeamMembersUseCase {

    private final TeamRepository teamRepository;
    private final CompanyRepository companyRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public ListTeamMembersUseCaseImpl(
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
    public Page<TeamMemberOutputDTO> execute(UUID companyId, UUID teamId, Pageable pageable)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        if (!companyRepository.isMember(currentUserId.get(), companyId)) {
            throw new ApplicationException("company.access.denied", null);
        }

        if (!teamRepository.existsActiveByIdAndCompanyId(teamId, companyId)) {
            throw new ApplicationException("team.not.found", null);
        }

        Page<TeamMembership> members = teamRepository
                .findApprovedMembershipsByTeamId(teamId, pageable);

        if (members.isEmpty()) {
            return members.map(m -> TeamMemberOutputDTO.from(m, null));
        }

        List<UUID> userIds = members.getContent().stream()
                .map(m -> m.getUserId())
                .distinct()
                .toList();

        Map<UUID, String> memberNames = personRepository.findNamesByIds(userIds);

        return members.map(membership -> TeamMemberOutputDTO.from(
                membership,
                memberNames.get(membership.getUserId())));
    }
}
