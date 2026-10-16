package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.input.ProjectFilterInputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectSummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.ListPersonalProjectsUseCase;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;
import com.vmarcante.time_tracker.core.shared.utils.PageableUtils;

@Service
public class ListPersonalProjectsUseCaseImpl implements ListPersonalProjectsUseCase {

    private final ProjectRepository projectRepository;
    private final SecurityContextPort securityContext;

    public ListPersonalProjectsUseCaseImpl(
            ProjectRepository projectRepository,
            SecurityContextPort securityContext) {
        this.projectRepository = projectRepository;
        this.securityContext = securityContext;
    }

    @Override
    public PageWrapperDTO<ProjectSummaryOutputDTO> execute(ProjectFilterInputDTO filter)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID userId = currentUserId.get();
        Pageable pageable = PageableUtils.pageable(filter.getPageNumber(), filter.getPageSize(), filter.getSort());

        return PageWrapperDTO.of(projectRepository.findActiveByUserId(userId, pageable)
                .map(project -> ProjectSummaryOutputDTO.from(project, 0, 0)));
    }
}
