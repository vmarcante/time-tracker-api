package com.vmarcante.time_tracker.core.interfaces.project.controllers;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.input.CreateProjectInputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.input.UpdateProjectInputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.CreateProjectOutputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectAssignmentOutputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectSummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.ArchiveProjectUseCase;
import com.vmarcante.time_tracker.core.application.project.in.CompleteProjectUseCase;
import com.vmarcante.time_tracker.core.application.project.in.CreateProjectUseCase;
import com.vmarcante.time_tracker.core.application.project.in.DeactivateProjectUseCase;
import com.vmarcante.time_tracker.core.application.project.in.FindProjectByIdUseCase;
import com.vmarcante.time_tracker.core.application.project.in.ListCompanyProjectsUseCase;
import com.vmarcante.time_tracker.core.application.project.in.ListMyProjectsUseCase;
import com.vmarcante.time_tracker.core.application.project.in.ListProjectMembersUseCase;
import com.vmarcante.time_tracker.core.application.project.in.ReactivateProjectUseCase;
import com.vmarcante.time_tracker.core.application.project.in.UpdateProjectUseCase;
import com.vmarcante.time_tracker.core.domain.user.auth.annotation.AuthSecure;
import com.vmarcante.time_tracker.core.shared.utils.PageableUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/companies/{companyId}/projects")
@Tag(name = "Project Management", description = "Project management endpoints")
public class ProjectController extends BaseResponseController {

    private final CreateProjectUseCase createProjectUseCase;
    private final UpdateProjectUseCase updateProjectUseCase;
    private final DeactivateProjectUseCase deactivateProjectUseCase;
    private final FindProjectByIdUseCase findProjectByIdUseCase;
    private final ListCompanyProjectsUseCase listCompanyProjectsUseCase;
    private final ListMyProjectsUseCase listMyProjectsUseCase;
    private final ListProjectMembersUseCase listProjectMembersUseCase;
    private final ArchiveProjectUseCase archiveProjectUseCase;
    private final CompleteProjectUseCase completeProjectUseCase;
    private final ReactivateProjectUseCase reactivateProjectUseCase;

    public ProjectController(
            CreateProjectUseCase createProjectUseCase,
            UpdateProjectUseCase updateProjectUseCase,
            DeactivateProjectUseCase deactivateProjectUseCase,
            FindProjectByIdUseCase findProjectByIdUseCase,
            ListCompanyProjectsUseCase listCompanyProjectsUseCase,
            ListMyProjectsUseCase listMyProjectsUseCase,
            ListProjectMembersUseCase listProjectMembersUseCase,
            ArchiveProjectUseCase archiveProjectUseCase,
            CompleteProjectUseCase completeProjectUseCase,
            ReactivateProjectUseCase reactivateProjectUseCase) {
        this.createProjectUseCase = createProjectUseCase;
        this.updateProjectUseCase = updateProjectUseCase;
        this.deactivateProjectUseCase = deactivateProjectUseCase;
        this.findProjectByIdUseCase = findProjectByIdUseCase;
        this.listCompanyProjectsUseCase = listCompanyProjectsUseCase;
        this.listMyProjectsUseCase = listMyProjectsUseCase;
        this.listProjectMembersUseCase = listProjectMembersUseCase;
        this.archiveProjectUseCase = archiveProjectUseCase;
        this.completeProjectUseCase = completeProjectUseCase;
        this.reactivateProjectUseCase = reactivateProjectUseCase;
    }

    @AuthSecure
    @PostMapping
    @Operation(summary = "Create project", description = "Creates a new project in the company (OWNER or ADMIN only)")
    public ResponseEntity<ApiResponseDTO<CreateProjectOutputDTO>> createProject(
            @PathVariable UUID companyId,
            @RequestBody CreateProjectInputDTO input) throws ApplicationException {
        return created(createProjectUseCase.execute(companyId, input));
    }

    @AuthSecure
    @GetMapping
    @Operation(summary = "List company projects", description = "Lists all active projects of the company")
    public ResponseEntity<ApiResponseDTO<PageWrapperDTO<ProjectSummaryOutputDTO>>> listProjects(
            @PathVariable UUID companyId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) throws ApplicationException {
        Pageable pageable = PageableUtils.pageable(page, size, null);
        return ok(PageWrapperDTO.of(listCompanyProjectsUseCase.execute(companyId, pageable)));
    }

    @AuthSecure
    @GetMapping("/mine")
    @Operation(summary = "My projects", description = "Lists the projects assigned to the authenticated user in this company")
    public ResponseEntity<ApiResponseDTO<PageWrapperDTO<ProjectSummaryOutputDTO>>> myProjects(
            @PathVariable UUID companyId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) throws ApplicationException {
        Pageable pageable = PageableUtils.pageable(page, size, null);
        return ok(PageWrapperDTO.of(listMyProjectsUseCase.execute(companyId, pageable)));
    }

    @AuthSecure
    @GetMapping("/{projectId}")
    @Operation(summary = "Find project by ID", description = "Returns project details (company members only)")
    public ResponseEntity<ApiResponseDTO<ProjectDetailOutputDTO>> findProject(
            @PathVariable UUID companyId,
            @PathVariable UUID projectId) throws ApplicationException {
        return ok(findProjectByIdUseCase.execute(companyId, projectId));
    }

    @AuthSecure
    @PutMapping("/{projectId}")
    @Operation(summary = "Update project", description = "Updates project name, description or dates (OWNER or ADMIN only)")
    public ResponseEntity<ApiResponseDTO<ProjectDetailOutputDTO>> updateProject(
            @PathVariable UUID companyId,
            @PathVariable UUID projectId,
            @RequestBody UpdateProjectInputDTO input) throws ApplicationException {
        return ok(updateProjectUseCase.execute(companyId, projectId, input));
    }

    @AuthSecure
    @DeleteMapping("/{projectId}")
    @Operation(summary = "Deactivate project", description = "Soft-deletes the project, its team links and member assignments (OWNER or ADMIN only)")
    public ResponseEntity<ApiResponseDTO<Void>> deactivateProject(
            @PathVariable UUID companyId,
            @PathVariable UUID projectId) throws ApplicationException {
        deactivateProjectUseCase.execute(companyId, projectId);
        return noContent();
    }

    @AuthSecure
    @PostMapping("/{projectId}/archive")
    @Operation(summary = "Archive project", description = "Moves the project to ARCHIVED status (OWNER or ADMIN only)")
    public ResponseEntity<ApiResponseDTO<ProjectDetailOutputDTO>> archiveProject(
            @PathVariable UUID companyId,
            @PathVariable UUID projectId) throws ApplicationException {
        return ok(archiveProjectUseCase.execute(companyId, projectId));
    }

    @AuthSecure
    @PostMapping("/{projectId}/complete")
    @Operation(summary = "Complete project", description = "Moves the project to COMPLETED status (OWNER or ADMIN only)")
    public ResponseEntity<ApiResponseDTO<ProjectDetailOutputDTO>> completeProject(
            @PathVariable UUID companyId,
            @PathVariable UUID projectId) throws ApplicationException {
        return ok(completeProjectUseCase.execute(companyId, projectId));
    }

    @AuthSecure
    @PostMapping("/{projectId}/reactivate")
    @Operation(summary = "Reactivate project", description = "Moves the project back to ACTIVE status (OWNER or ADMIN only)")
    public ResponseEntity<ApiResponseDTO<ProjectDetailOutputDTO>> reactivateProject(
            @PathVariable UUID companyId,
            @PathVariable UUID projectId) throws ApplicationException {
        return ok(reactivateProjectUseCase.execute(companyId, projectId));
    }

    @AuthSecure
    @GetMapping("/{projectId}/members")
    @Operation(summary = "List project members", description = "Lists all member assignments of the project across teams")
    public ResponseEntity<ApiResponseDTO<PageWrapperDTO<ProjectAssignmentOutputDTO>>> listMembers(
            @PathVariable UUID companyId,
            @PathVariable UUID projectId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) throws ApplicationException {
        Pageable pageable = PageableUtils.pageable(page, size, null);
        return ok(PageWrapperDTO.of(listProjectMembersUseCase.execute(companyId, projectId, pageable)));
    }
}
