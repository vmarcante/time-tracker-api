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
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectSummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.ChangePersonalProjectStatusUseCase;
import com.vmarcante.time_tracker.core.application.project.in.CreatePersonalProjectUseCase;
import com.vmarcante.time_tracker.core.application.project.in.DeactivatePersonalProjectUseCase;
import com.vmarcante.time_tracker.core.application.project.in.FindPersonalProjectByIdUseCase;
import com.vmarcante.time_tracker.core.application.project.in.ListPersonalProjectsUseCase;
import com.vmarcante.time_tracker.core.application.project.in.UpdatePersonalProjectUseCase;
import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;
import com.vmarcante.time_tracker.core.domain.user.auth.annotation.AuthSecure;
import com.vmarcante.time_tracker.core.shared.utils.PageableUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/projects")
@Tag(name = "Personal Projects", description = "Personal project management endpoints")
public class PersonalProjectController extends BaseResponseController {

    private final CreatePersonalProjectUseCase createPersonalProjectUseCase;
    private final UpdatePersonalProjectUseCase updatePersonalProjectUseCase;
    private final DeactivatePersonalProjectUseCase deactivatePersonalProjectUseCase;
    private final FindPersonalProjectByIdUseCase findPersonalProjectByIdUseCase;
    private final ListPersonalProjectsUseCase listPersonalProjectsUseCase;
    private final ChangePersonalProjectStatusUseCase changePersonalProjectStatusUseCase;

    public PersonalProjectController(
            CreatePersonalProjectUseCase createPersonalProjectUseCase,
            UpdatePersonalProjectUseCase updatePersonalProjectUseCase,
            DeactivatePersonalProjectUseCase deactivatePersonalProjectUseCase,
            FindPersonalProjectByIdUseCase findPersonalProjectByIdUseCase,
            ListPersonalProjectsUseCase listPersonalProjectsUseCase,
            ChangePersonalProjectStatusUseCase changePersonalProjectStatusUseCase) {
        this.createPersonalProjectUseCase = createPersonalProjectUseCase;
        this.updatePersonalProjectUseCase = updatePersonalProjectUseCase;
        this.deactivatePersonalProjectUseCase = deactivatePersonalProjectUseCase;
        this.findPersonalProjectByIdUseCase = findPersonalProjectByIdUseCase;
        this.listPersonalProjectsUseCase = listPersonalProjectsUseCase;
        this.changePersonalProjectStatusUseCase = changePersonalProjectStatusUseCase;
    }

    @AuthSecure
    @PostMapping
    @Operation(summary = "Create personal project", description = "Creates a project owned by the authenticated user")
    public ResponseEntity<ApiResponseDTO<CreateProjectOutputDTO>> create(
            @RequestBody CreateProjectInputDTO input) throws ApplicationException {
        return created(createPersonalProjectUseCase.execute(input));
    }

    @AuthSecure
    @GetMapping
    @Operation(summary = "List my personal projects", description = "Lists active projects owned by the authenticated user")
    public ResponseEntity<ApiResponseDTO<PageWrapperDTO<ProjectSummaryOutputDTO>>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) throws ApplicationException {
        Pageable pageable = PageableUtils.pageable(page, size, null);
        return ok(PageWrapperDTO.of(listPersonalProjectsUseCase.execute(pageable)));
    }

    @AuthSecure
    @GetMapping("/{projectId}")
    @Operation(summary = "Get personal project", description = "Returns details of a project owned by the authenticated user")
    public ResponseEntity<ApiResponseDTO<ProjectDetailOutputDTO>> findById(
            @PathVariable UUID projectId) throws ApplicationException {
        return ok(findPersonalProjectByIdUseCase.execute(projectId));
    }

    @AuthSecure
    @PutMapping("/{projectId}")
    @Operation(summary = "Update personal project", description = "Updates a project owned by the authenticated user")
    public ResponseEntity<ApiResponseDTO<ProjectDetailOutputDTO>> update(
            @PathVariable UUID projectId,
            @RequestBody UpdateProjectInputDTO input) throws ApplicationException {
        return ok(updatePersonalProjectUseCase.execute(projectId, input));
    }

    @AuthSecure
    @DeleteMapping("/{projectId}")
    @Operation(summary = "Deactivate personal project", description = "Deactivates a project owned by the authenticated user")
    public ResponseEntity<ApiResponseDTO<Void>> deactivate(
            @PathVariable UUID projectId) throws ApplicationException {
        deactivatePersonalProjectUseCase.execute(projectId);
        return noContent();
    }

    @AuthSecure
    @PostMapping("/{projectId}/archive")
    @Operation(summary = "Archive personal project", description = "Archives a personal project")
    public ResponseEntity<ApiResponseDTO<ProjectDetailOutputDTO>> archive(
            @PathVariable UUID projectId) throws ApplicationException {
        return ok(changePersonalProjectStatusUseCase.execute(projectId, ProjectStatus.ARCHIVED));
    }

    @AuthSecure
    @PostMapping("/{projectId}/complete")
    @Operation(summary = "Complete personal project", description = "Marks a personal project as completed")
    public ResponseEntity<ApiResponseDTO<ProjectDetailOutputDTO>> complete(
            @PathVariable UUID projectId) throws ApplicationException {
        return ok(changePersonalProjectStatusUseCase.execute(projectId, ProjectStatus.COMPLETED));
    }

    @AuthSecure
    @PostMapping("/{projectId}/reactivate")
    @Operation(summary = "Reactivate personal project", description = "Reactivates an archived or completed personal project")
    public ResponseEntity<ApiResponseDTO<ProjectDetailOutputDTO>> reactivate(
            @PathVariable UUID projectId) throws ApplicationException {
        return ok(changePersonalProjectStatusUseCase.execute(projectId, ProjectStatus.ACTIVE));
    }
}
