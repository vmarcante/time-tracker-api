package com.vmarcante.time_tracker.core.application.project.member.use_cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectAssignmentFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.base.application.pagination.PageableUtils;
import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.company.membership.dto.input.MembershipFilterInputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.member.dto.output.ProjectAssignmentOutputDTO;
import com.vmarcante.time_tracker.core.application.project.member.in.ListProjectMembersUseCase;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.model.ProjectAssignment;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectAssignmentFilter;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListProjectMembersUseCaseImpl implements ListProjectMembersUseCase {

    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final PersonRepository personRepository;
    private final SecurityContextPort securityContext;

    public ListProjectMembersUseCaseImpl(
            ProjectRepository projectRepository,
            ProjectAssignmentRepository projectAssignmentRepository,
            CompanyMembershipRepository membershipRepository,
            PersonRepository personRepository,
            SecurityContextPort securityContext) {
        this.projectRepository = projectRepository;
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.membershipRepository = membershipRepository;
        this.personRepository = personRepository;
        this.securityContext = securityContext;
    }

    @Override
    public PageWrapperDTO<ProjectAssignmentOutputDTO> execute(UUID companyId, UUID projectId,
            MembershipFilterInputDTO filter) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(currentUserId.get(), companyId);
        if (!membershipRepository.exists(companyMembershipFilter)) {
            throw new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN);
        }

        Project project = projectRepository.findById(projectId)
                .filter(p -> companyId.equals(p.getCompanyId()))
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new ApplicationException("project.not.found", null, HttpStatus.NOT_FOUND));

        Pageable pageable = PageableUtils.pageable(filter.getPageNumber(), filter.getPageSize(), filter.getSort());
        ProjectAssignmentFilter assignmentFilter = ProjectAssignmentFilterMapper.activeByProjectId(projectId);
        Page<ProjectAssignment> assignments = projectAssignmentRepository.search(assignmentFilter, pageable);

        if (assignments.isEmpty()) {
            return PageWrapperDTO.of(assignments.map(a -> ProjectAssignmentOutputDTO.from(a, null, project.getName())));
        }

        List<UUID> userIds = assignments.getContent().stream()
                .map(a -> a.getUserId())
                .distinct()
                .toList();
        Map<UUID, String> userNames = personRepository.findNamesByIds(userIds);

        return PageWrapperDTO.of(assignments.map(a -> ProjectAssignmentOutputDTO.from(
                a, userNames.get(a.getUserId()), project.getName())));
    }
}
