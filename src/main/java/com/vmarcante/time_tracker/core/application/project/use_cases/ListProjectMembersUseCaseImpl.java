package com.vmarcante.time_tracker.core.application.project.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectAssignmentOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.ListProjectMembersUseCase;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.model.ProjectAssignment;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListProjectMembersUseCaseImpl implements ListProjectMembersUseCase {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public ListProjectMembersUseCaseImpl(
            ProjectRepository projectRepository,
            CompanyRepository companyRepository,
            PersonRepository personRepository,
            SecurityContextPort securityContext) {
        this.projectRepository = projectRepository;
        this.companyRepository = companyRepository;
        this.personRepository = personRepository;
        this.securityContext = securityContext;
    }

    @Override
    public Page<ProjectAssignmentOutputDTO> execute(UUID companyId, UUID projectId, Pageable pageable)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        if (!companyRepository.isMember(currentUserId.get(), companyId)) {
            throw new ApplicationException("company.access.denied", null);
        }

        Project project = projectRepository.findById(projectId)
                .filter(p -> companyId.equals(p.getCompanyId()))
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new ApplicationException("project.not.found", null));

        Page<ProjectAssignment> assignments = projectRepository
                .findActiveAssignmentsByProjectId(projectId, pageable);

        if (assignments.isEmpty()) {
            return assignments.map(a -> ProjectAssignmentOutputDTO.from(a, null, project.getName()));
        }

        List<UUID> userIds = assignments.getContent().stream()
                .map(a -> a.getUserId())
                .distinct()
                .toList();
        Map<UUID, String> userNames = personRepository.findNamesByIds(userIds);

        return assignments.map(a -> ProjectAssignmentOutputDTO.from(
                a, userNames.get(a.getUserId()), project.getName()));
    }
}
