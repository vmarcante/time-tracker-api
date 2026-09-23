package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.in.DeactivateProjectUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DeactivateProjectUseCaseImpl implements DeactivateProjectUseCase {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final SecurityContextPort securityContext;

    public DeactivateProjectUseCaseImpl(
            ProjectRepository projectRepository,
            CompanyRepository companyRepository,
            SecurityContextPort securityContext) {
        this.projectRepository = projectRepository;
        this.companyRepository = companyRepository;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public void execute(UUID companyId, UUID projectId) throws ApplicationException {
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

        Project project = projectRepository.findById(projectId)
                .filter(p -> p.getCompanyId().equals(companyId))
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new ApplicationException("project.not.found", null));

        projectRepository.deactivateAssignmentsByProjectId(projectId, actorId);
        projectRepository.deactivateTeamLinksByProjectId(projectId, actorId);

        project.setActive(false);
        project.setUpdatedBy(actorId);
        projectRepository.save(project);

        log.info("[Deactivate Project] Project {} deactivated by {} "
                + "(team links and member assignments deactivated)", projectId, actorId);
    }
}
