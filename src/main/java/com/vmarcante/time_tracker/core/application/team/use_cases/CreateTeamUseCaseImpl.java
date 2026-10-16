package com.vmarcante.time_tracker.core.application.team.use_cases;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.input.CreateTeamInputDTO;
import com.vmarcante.time_tracker.core.application.team.dto.output.CreateTeamOutputDTO;
import com.vmarcante.time_tracker.core.application.team.in.CreateTeamUseCase;
import com.vmarcante.time_tracker.core.application.team.policy.TeamValidationPolicy;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.team.model.Team;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilter;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CreateTeamUseCaseImpl implements CreateTeamUseCase {

    private final TeamRepository teamRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final TeamValidationPolicy validationPolicy;
    private final SecurityContextPort securityContext;

    public CreateTeamUseCaseImpl(
            TeamRepository teamRepository,
            CompanyMembershipRepository membershipRepository,
            TeamValidationPolicy validationPolicy,
            SecurityContextPort securityContext) {
        this.teamRepository = teamRepository;
        this.membershipRepository = membershipRepository;
        this.validationPolicy = validationPolicy;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public CreateTeamOutputDTO execute(UUID companyId, CreateTeamInputDTO input) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID actorId = currentUserId.get();

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(actorId, companyId);
        CompanyMembership actorMembership = membershipRepository.findOne(companyMembershipFilter)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN));

        if (!actorMembership.getRole().canManage(CompanyRole.MANAGER)) {
            throw new ApplicationException("company.permission.denied", null, HttpStatus.FORBIDDEN);
        }

        validationPolicy.validateName(input.name());

        TeamFilter nameFilter = TeamFilterMapper.activeByNameAndCompanyId(input.name().trim(), companyId);
        if (teamRepository.exists(nameFilter)) {
            throw new ApplicationException("team.name.already.exists", null, HttpStatus.CONFLICT);
        }

        Team team = new Team();
        team.setCompanyId(companyId);
        team.setName(input.name().trim());
        team.setDescription(input.description());
        team.setActive(true);
        team.setCreatedBy(actorId);
        team.setUpdatedBy(actorId);

        Team saved = teamRepository.save(team);

        log.info("[Create Team] Team {} created in company {} by {}", saved.getId(), companyId, actorId);

        return CreateTeamOutputDTO.from(saved);
    }
}
