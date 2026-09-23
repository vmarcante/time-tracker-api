package com.vmarcante.time_tracker.core.interfaces.project.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectAssignmentOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.GetUnlinkImpactUseCase;
import com.vmarcante.time_tracker.core.application.project.in.LinkProjectToTeamUseCase;
import com.vmarcante.time_tracker.core.application.project.in.ListProjectTeamsUseCase;
import com.vmarcante.time_tracker.core.application.project.in.UnlinkProjectFromTeamUseCase;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamSummaryOutputDTO;
import com.vmarcante.time_tracker.core.domain.user.auth.annotation.AuthSecure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/companies/{companyId}/projects/{projectId}/teams")
@Tag(name = "Project Team Links", description = "Project to team link management")
public class ProjectTeamLinkController extends BaseResponseController {

    private final LinkProjectToTeamUseCase linkProjectToTeamUseCase;
    private final UnlinkProjectFromTeamUseCase unlinkProjectFromTeamUseCase;
    private final ListProjectTeamsUseCase listProjectTeamsUseCase;
    private final GetUnlinkImpactUseCase getUnlinkImpactUseCase;

    public ProjectTeamLinkController(
            LinkProjectToTeamUseCase linkProjectToTeamUseCase,
            UnlinkProjectFromTeamUseCase unlinkProjectFromTeamUseCase,
            ListProjectTeamsUseCase listProjectTeamsUseCase,
            GetUnlinkImpactUseCase getUnlinkImpactUseCase) {
        this.linkProjectToTeamUseCase = linkProjectToTeamUseCase;
        this.unlinkProjectFromTeamUseCase = unlinkProjectFromTeamUseCase;
        this.listProjectTeamsUseCase = listProjectTeamsUseCase;
        this.getUnlinkImpactUseCase = getUnlinkImpactUseCase;
    }

    @AuthSecure
    @PostMapping("/{teamId}")
    @Operation(summary = "Link project to team", description = "Links a project to a team (OWNER or ADMIN only)")
    public ResponseEntity<ApiResponseDTO<Void>> linkTeam(
            @PathVariable UUID companyId,
            @PathVariable UUID projectId,
            @PathVariable UUID teamId) throws ApplicationException {
        linkProjectToTeamUseCase.execute(companyId, projectId, teamId);
        return noContent();
    }

    @AuthSecure
    @GetMapping
    @Operation(summary = "List project teams", description = "Lists all teams linked to the project")
    public ResponseEntity<ApiResponseDTO<List<TeamSummaryOutputDTO>>> listTeams(
            @PathVariable UUID companyId,
            @PathVariable UUID projectId) throws ApplicationException {
        return ok(listProjectTeamsUseCase.execute(companyId, projectId));
    }

    @AuthSecure
    @GetMapping("/{teamId}/unlink-impact")
    @Operation(summary = "Unlink impact preview", description = "Lists the member assignments that would be deactivated if the project is unlinked from the team")
    public ResponseEntity<ApiResponseDTO<List<ProjectAssignmentOutputDTO>>> unlinkImpact(
            @PathVariable UUID companyId,
            @PathVariable UUID projectId,
            @PathVariable UUID teamId) throws ApplicationException {
        return ok(getUnlinkImpactUseCase.execute(companyId, projectId, teamId));
    }

    @AuthSecure
    @DeleteMapping("/{teamId}")
    @Operation(summary = "Unlink project from team", description = "Removes the project-team link and deactivates the related member assignments (OWNER or ADMIN only)")
    public ResponseEntity<ApiResponseDTO<Void>> unlinkTeam(
            @PathVariable UUID companyId,
            @PathVariable UUID projectId,
            @PathVariable UUID teamId) throws ApplicationException {
        unlinkProjectFromTeamUseCase.execute(companyId, projectId, teamId);
        return noContent();
    }
}
