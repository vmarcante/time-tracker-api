package com.vmarcante.time_tracker.core.application.team.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.input.UpdateTeamInputDTO;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.team.in.UpdateTeamUseCase;
import com.vmarcante.time_tracker.core.application.team.policy.TeamValidationPolicy;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.team.model.Team;
import com.vmarcante.time_tracker.core.domain.team.model.TeamMembership;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UpdateTeamUseCaseImpl implements UpdateTeamUseCase {

    private final TeamRepository teamRepository;
    private final CompanyRepository companyRepository;
    private final PersonRepository personRepository;
    private final TeamValidationPolicy validationPolicy;
    private final SecurityContextPort securityContext;

    public UpdateTeamUseCaseImpl(
            TeamRepository teamRepository,
            CompanyRepository companyRepository,
            PersonRepository personRepository,
            TeamValidationPolicy validationPolicy,
            SecurityContextPort securityContext) {
        this.teamRepository = teamRepository;
        this.companyRepository = companyRepository;
        this.personRepository = personRepository;
        this.validationPolicy = validationPolicy;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public TeamDetailOutputDTO execute(UUID companyId, UUID teamId, UpdateTeamInputDTO input)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID actorId = currentUserId.get();

        CompanyMembership actorMembership = companyRepository.findMembership(actorId, companyId)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null));

        if (!actorMembership.getRole().canManage(CompanyRole.MANAGER)) {
            throw new ApplicationException("company.permission.denied", null);
        }

        Team team = teamRepository.findById(teamId)
                .filter(t -> t.getCompanyId().equals(companyId))
                .filter(t -> Boolean.TRUE.equals(t.getActive()))
                .orElseThrow(() -> new ApplicationException("team.not.found", null));

        if (input.name() != null && !input.name().isBlank()) {
            validationPolicy.validateName(input.name());
            String newName = input.name().trim();
            if (!newName.equalsIgnoreCase(team.getName())
                    && teamRepository.existsActiveByCompanyIdAndName(companyId, newName)) {
                throw new ApplicationException("team.name.already.exists", null);
            }
            team.setName(newName);
        }
        if (input.description() != null) {
            team.setDescription(input.description().isBlank() ? null : input.description());
        }
        team.setUpdatedBy(actorId);

        Team saved = teamRepository.save(team);

        log.info("[Update Team] Team {} updated by {}", teamId, actorId);

        return buildDetail(saved);
    }

    private TeamDetailOutputDTO buildDetail(Team team) {
        Optional<TeamMembership> lead = teamRepository.findLeadMembership(team.getId());
        UUID leadUserId = lead.isPresent() ? lead.get().getUserId() : null;
        String leadName = leadUserId != null
                ? personRepository.findNameById(leadUserId).orElse(null)
                : null;

        Map<UUID, Long> counts = teamRepository.countApprovedMembersByTeamIds(List.of(team.getId()));
        long memberCount = counts.getOrDefault(team.getId(), 0L);

        return TeamDetailOutputDTO.from(team, leadUserId, leadName, memberCount);
    }
}
