package com.vmarcante.time_tracker.core.application.team.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.input.CreateTeamInputDTO;
import com.vmarcante.time_tracker.core.application.team.dto.output.CreateTeamOutputDTO;
import com.vmarcante.time_tracker.core.application.team.in.CreateTeamUseCase;
import com.vmarcante.time_tracker.core.application.team.policy.TeamValidationPolicy;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.team.model.Team;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CreateTeamUseCaseImpl implements CreateTeamUseCase {

    private final TeamRepository teamRepository;
    private final CompanyRepository companyRepository;
    private final TeamValidationPolicy validationPolicy;
    private final SecurityContextPort securityContext;

    public CreateTeamUseCaseImpl(
            TeamRepository teamRepository,
            CompanyRepository companyRepository,
            TeamValidationPolicy validationPolicy,
            SecurityContextPort securityContext) {
        this.teamRepository = teamRepository;
        this.companyRepository = companyRepository;
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

        CompanyMembership actorMembership = companyRepository.findMembership(actorId, companyId)
                .orElseThrow(() -> new ApplicationException("company.access.denied", null));

        if (!actorMembership.getRole().canManage(CompanyRole.MANAGER)) {
            throw new ApplicationException("company.permission.denied", null);
        }

        validationPolicy.validateName(input.name());

        if (teamRepository.existsActiveByCompanyIdAndName(companyId, input.name().trim())) {
            throw new ApplicationException("team.name.already.exists", null);
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
