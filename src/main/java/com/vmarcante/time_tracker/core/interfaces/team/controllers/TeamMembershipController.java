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
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectAssignmentOutputDTO;
import com.vmarcante.time_tracker.core.application.project.in.GetMemberRemovalImpactUseCase;
import com.vmarcante.time_tracker.core.application.team.dto.input.TeamMemberEmailInputDTO;
import com.vmarcante.time_tracker.core.application.team.dto.output.TeamMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.team.in.AddTeamMemberUseCase;
import com.vmarcante.time_tracker.core.application.team.in.AssignTeamLeadUseCase;
import com.vmarcante.time_tracker.core.application.team.in.LeaveTeamUseCase;
import com.vmarcante.time_tracker.core.application.team.in.ListTeamMembersUseCase;
import com.vmarcante.time_tracker.core.application.team.in.RemoveTeamMemberUseCase;
import com.vmarcante.time_tracker.core.domain.user.auth.annotation.AuthSecure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/companies/{companyId}/teams")
@Tag(name = "Team Membership", description = "Team membership endpoints")
public class TeamMembershipController extends BaseResponseController {

    private final AddTeamMemberUseCase addTeamMemberUseCase;
    private final RemoveTeamMemberUseCase removeTeamMemberUseCase;
    private final ListTeamMembersUseCase listTeamMembersUseCase;
    private final AssignTeamLeadUseCase assignTeamLeadUseCase;
    private final LeaveTeamUseCase leaveTeamUseCase;
    private final GetMemberRemovalImpactUseCase getMemberRemovalImpactUseCase;

    public TeamMembershipController(
            AddTeamMemberUseCase addTeamMemberUseCase,
            RemoveTeamMemberUseCase removeTeamMemberUseCase,
            ListTeamMembersUseCase listTeamMembersUseCase,
            AssignTeamLeadUseCase assignTeamLeadUseCase,
            LeaveTeamUseCase leaveTeamUseCase,
            GetMemberRemovalImpactUseCase getMemberRemovalImpactUseCase) {
        this.addTeamMemberUseCase = addTeamMemberUseCase;
        this.removeTeamMemberUseCase = removeTeamMemberUseCase;
        this.listTeamMembersUseCase = listTeamMembersUseCase;
        this.assignTeamLeadUseCase = assignTeamLeadUseCase;
        this.leaveTeamUseCase = leaveTeamUseCase;
        this.getMemberRemovalImpactUseCase = getMemberRemovalImpactUseCase;
    }

    @AuthSecure
    @PostMapping("/{teamId}/members")
    @Operation(summary = "Add member", description = "Adds a company member to the team (OWNER/ADMIN or the team LEAD)")
    public ResponseEntity<ApiResponseDTO<TeamMemberOutputDTO>> addMember(
            @PathVariable UUID companyId,
            @PathVariable UUID teamId,
            @RequestBody TeamMemberEmailInputDTO input) throws ApplicationException {
        return created(addTeamMemberUseCase.execute(companyId, teamId, input));
    }

    @AuthSecure
    @GetMapping("/{teamId}/members")
    @Operation(summary = "List members", description = "Lists all members of the team (company members only)")
    public ResponseEntity<ApiResponseDTO<List<TeamMemberOutputDTO>>> listMembers(
            @PathVariable UUID companyId,
            @PathVariable UUID teamId) throws ApplicationException {
        return ok(listTeamMembersUseCase.execute(companyId, teamId));
    }

    @AuthSecure
    @GetMapping("/{teamId}/members/{membershipId}/removal-impact")
    @Operation(summary = "Member removal impact", description = "Lists the project assignments that would be deactivated if the member is removed from the team")
    public ResponseEntity<ApiResponseDTO<List<ProjectAssignmentOutputDTO>>> removalImpact(
            @PathVariable UUID companyId,
            @PathVariable UUID teamId,
            @PathVariable UUID membershipId) throws ApplicationException {
        return ok(getMemberRemovalImpactUseCase.execute(companyId, teamId, membershipId));
    }

    @AuthSecure
    @DeleteMapping("/{teamId}/members/{membershipId}")
    @Operation(summary = "Remove member", description = "Removes a member from the team (OWNER/ADMIN or the team LEAD)")
    public ResponseEntity<ApiResponseDTO<Void>> removeMember(
            @PathVariable UUID companyId,
            @PathVariable UUID teamId,
            @PathVariable UUID membershipId) throws ApplicationException {
        removeTeamMemberUseCase.execute(companyId, teamId, membershipId);
        return noContent();
    }

    @AuthSecure
    @PutMapping("/{teamId}/lead")
    @Operation(summary = "Assign lead", description = "Assigns a company MANAGER as the team lead (OWNER or ADMIN only). Previous lead becomes MEMBER")
    public ResponseEntity<ApiResponseDTO<TeamMemberOutputDTO>> assignLead(
            @PathVariable UUID companyId,
            @PathVariable UUID teamId,
            @RequestBody TeamMemberEmailInputDTO input) throws ApplicationException {
        return ok(assignTeamLeadUseCase.execute(companyId, teamId, input));
    }

    @AuthSecure
    @PostMapping("/{teamId}/leave")
    @Operation(summary = "Leave team", description = "Leaves the team (the team may end up without a lead)")
    public ResponseEntity<ApiResponseDTO<Void>> leaveTeam(
            @PathVariable UUID companyId,
            @PathVariable UUID teamId) throws ApplicationException {
        leaveTeamUseCase.execute(companyId, teamId);
        return noContent();
    }
}
