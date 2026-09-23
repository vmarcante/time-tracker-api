package com.vmarcante.time_tracker.core.interfaces.project.controllers;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.input.ProjectMemberEmailInputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectAssignmentOutputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectSummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.AssignProjectMemberUseCase;
import com.vmarcante.time_tracker.core.application.project.in.ListTeamProjectsUseCase;
import com.vmarcante.time_tracker.core.application.project.in.UnassignProjectMemberUseCase;
import com.vmarcante.time_tracker.core.domain.user.auth.annotation.AuthSecure;
import com.vmarcante.time_tracker.core.shared.utils.PageableUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/companies/{companyId}/teams/{teamId}/projects")
@Tag(name = "Project Assignments", description = "Team project listing and member assignment endpoints")
public class ProjectAssignmentController extends BaseResponseController {

    private final ListTeamProjectsUseCase listTeamProjectsUseCase;
    private final AssignProjectMemberUseCase assignProjectMemberUseCase;
    private final UnassignProjectMemberUseCase unassignProjectMemberUseCase;

    public ProjectAssignmentController(
            ListTeamProjectsUseCase listTeamProjectsUseCase,
            AssignProjectMemberUseCase assignProjectMemberUseCase,
            UnassignProjectMemberUseCase unassignProjectMemberUseCase) {
        this.listTeamProjectsUseCase = listTeamProjectsUseCase;
        this.assignProjectMemberUseCase = assignProjectMemberUseCase;
        this.unassignProjectMemberUseCase = unassignProjectMemberUseCase;
    }

    @AuthSecure
    @GetMapping
    @Operation(summary = "List team projects", description = "Lists all projects linked to the team")
    public ResponseEntity<ApiResponseDTO<PageWrapperDTO<ProjectSummaryOutputDTO>>> listTeamProjects(
            @PathVariable UUID companyId,
            @PathVariable UUID teamId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) throws ApplicationException {
        Pageable pageable = PageableUtils.pageable(page, size, null);
        return ok(PageWrapperDTO.of(listTeamProjectsUseCase.execute(companyId, teamId, pageable)));
    }

    @AuthSecure
    @PostMapping("/{projectId}/members")
    @Operation(summary = "Assign member to project", description = "Assigns a team member to a project linked to the team (OWNER/ADMIN or the team LEAD)")
    public ResponseEntity<ApiResponseDTO<ProjectAssignmentOutputDTO>> assignMember(
            @PathVariable UUID companyId,
            @PathVariable UUID teamId,
            @PathVariable UUID projectId,
            @RequestBody ProjectMemberEmailInputDTO input) throws ApplicationException {
        return created(assignProjectMemberUseCase.execute(companyId, teamId, projectId, input));
    }

    @AuthSecure
    @DeleteMapping("/{projectId}/members/{assignmentId}")
    @Operation(summary = "Unassign member from project", description = "Removes a member assignment (OWNER/ADMIN or the team LEAD)")
    public ResponseEntity<ApiResponseDTO<Void>> unassignMember(
            @PathVariable UUID companyId,
            @PathVariable UUID teamId,
            @PathVariable UUID projectId,
            @PathVariable UUID assignmentId) throws ApplicationException {
        unassignProjectMemberUseCase.execute(companyId, teamId, projectId, assignmentId);
        return noContent();
    }
}
