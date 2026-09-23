package com.vmarcante.time_tracker.core.interfaces.team.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.team.dto.input.CreateTeamInputDTO;
import com.vmarcante.time_tracker.core.application.team.dto.input.UpdateTeamInputDTO;
import com.vmarcante.time_tracker.core.application.team.dto.output.CreateTeamOutputDTO;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamSummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.team.in.CreateTeamUseCase;
import com.vmarcante.time_tracker.core.application.team.in.DeactivateTeamUseCase;
import com.vmarcante.time_tracker.core.application.team.in.FindTeamByIdUseCase;
import com.vmarcante.time_tracker.core.application.team.in.ListCompanyTeamsUseCase;
import com.vmarcante.time_tracker.core.application.team.in.ListMyTeamsUseCase;
import com.vmarcante.time_tracker.core.application.team.in.UpdateTeamUseCase;
import com.vmarcante.time_tracker.core.domain.user.auth.annotation.AuthSecure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/companies/{companyId}/teams")
@Tag(name = "Team Management", description = "Team management endpoints")
public class TeamController extends BaseResponseController {

    private final CreateTeamUseCase createTeamUseCase;
    private final UpdateTeamUseCase updateTeamUseCase;
    private final DeactivateTeamUseCase deactivateTeamUseCase;
    private final FindTeamByIdUseCase findTeamByIdUseCase;
    private final ListCompanyTeamsUseCase listCompanyTeamsUseCase;
    private final ListMyTeamsUseCase listMyTeamsUseCase;

    public TeamController(
            CreateTeamUseCase createTeamUseCase,
            UpdateTeamUseCase updateTeamUseCase,
            DeactivateTeamUseCase deactivateTeamUseCase,
            FindTeamByIdUseCase findTeamByIdUseCase,
            ListCompanyTeamsUseCase listCompanyTeamsUseCase,
            ListMyTeamsUseCase listMyTeamsUseCase) {
        this.createTeamUseCase = createTeamUseCase;
        this.updateTeamUseCase = updateTeamUseCase;
        this.deactivateTeamUseCase = deactivateTeamUseCase;
        this.findTeamByIdUseCase = findTeamByIdUseCase;
        this.listCompanyTeamsUseCase = listCompanyTeamsUseCase;
        this.listMyTeamsUseCase = listMyTeamsUseCase;
    }

    @AuthSecure
    @PostMapping
    @Operation(summary = "Create team", description = "Creates a new team in the company (OWNER or ADMIN only)")
    public ResponseEntity<ApiResponseDTO<CreateTeamOutputDTO>> createTeam(
            @PathVariable UUID companyId,
            @RequestBody CreateTeamInputDTO input) throws ApplicationException {
        return created(createTeamUseCase.execute(companyId, input));
    }

    @AuthSecure
    @GetMapping
    @Operation(summary = "List company teams", description = "Lists all active teams of the company")
    public ResponseEntity<ApiResponseDTO<List<TeamSummaryOutputDTO>>> listTeams(
            @PathVariable UUID companyId) throws ApplicationException {
        return ok(listCompanyTeamsUseCase.execute(companyId));
    }

    @AuthSecure
    @GetMapping("/mine")
    @Operation(summary = "My teams", description = "Lists the teams the authenticated user belongs to in this company")
    public ResponseEntity<ApiResponseDTO<List<TeamSummaryOutputDTO>>> myTeams(
            @PathVariable UUID companyId) throws ApplicationException {
        return ok(listMyTeamsUseCase.execute(companyId));
    }

    @AuthSecure
    @GetMapping("/{teamId}")
    @Operation(summary = "Find team by ID", description = "Returns team details (company members only)")
    public ResponseEntity<ApiResponseDTO<TeamDetailOutputDTO>> findTeam(
            @PathVariable UUID companyId,
            @PathVariable UUID teamId) throws ApplicationException {
        return ok(findTeamByIdUseCase.execute(companyId, teamId));
    }

    @AuthSecure
    @PutMapping("/{teamId}")
    @Operation(summary = "Update team", description = "Updates team name or description (OWNER or ADMIN only)")
    public ResponseEntity<ApiResponseDTO<TeamDetailOutputDTO>> updateTeam(
            @PathVariable UUID companyId,
            @PathVariable UUID teamId,
            @RequestBody UpdateTeamInputDTO input) throws ApplicationException {
        return ok(updateTeamUseCase.execute(companyId, teamId, input));
    }

    @AuthSecure
    @DeleteMapping("/{teamId}")
    @Operation(summary = "Deactivate team", description = "Soft-deletes the team and its memberships (OWNER or ADMIN only)")
    public ResponseEntity<ApiResponseDTO<Void>> deactivateTeam(
            @PathVariable UUID companyId,
            @PathVariable UUID teamId) throws ApplicationException {
        deactivateTeamUseCase.execute(companyId, teamId);
        return noContent();
    }
}
