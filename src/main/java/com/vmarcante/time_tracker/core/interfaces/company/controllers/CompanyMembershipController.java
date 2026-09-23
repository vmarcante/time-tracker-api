package com.vmarcante.time_tracker.core.interfaces.company.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.company.dto.input.ChangeMemberRoleInputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.input.InviteMemberInputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyInvitationOutputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.AcceptCompanyInvitationUseCase;
import com.vmarcante.time_tracker.core.application.company.in.ApproveMembershipRequestUseCase;
import com.vmarcante.time_tracker.core.application.company.in.ChangeMemberRoleUseCase;
import com.vmarcante.time_tracker.core.application.company.in.DeclineCompanyInvitationUseCase;
import com.vmarcante.time_tracker.core.application.company.in.InviteMemberUseCase;
import com.vmarcante.time_tracker.core.application.company.in.LeaveCompanyUseCase;
import com.vmarcante.time_tracker.core.application.company.in.ListCompanyMembersUseCase;
import com.vmarcante.time_tracker.core.application.company.in.ListMyInvitationsUseCase;
import com.vmarcante.time_tracker.core.application.company.in.ListPendingMembershipsUseCase;
import com.vmarcante.time_tracker.core.application.company.in.RejectMembershipRequestUseCase;
import com.vmarcante.time_tracker.core.application.company.in.RemoveMemberUseCase;
import com.vmarcante.time_tracker.core.application.company.in.RequestToJoinCompanyUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.user.auth.annotation.AuthSecure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/companies")
@Tag(name = "Company Membership", description = "Company membership and approval flow endpoints")
public class CompanyMembershipController extends BaseResponseController {

    private final InviteMemberUseCase inviteMemberUseCase;
    private final RequestToJoinCompanyUseCase requestToJoinCompanyUseCase;
    private final ApproveMembershipRequestUseCase approveMembershipRequestUseCase;
    private final RejectMembershipRequestUseCase rejectMembershipRequestUseCase;
    private final AcceptCompanyInvitationUseCase acceptCompanyInvitationUseCase;
    private final DeclineCompanyInvitationUseCase declineCompanyInvitationUseCase;
    private final ListMyInvitationsUseCase listMyInvitationsUseCase;
    private final ListCompanyMembersUseCase listCompanyMembersUseCase;
    private final ListPendingMembershipsUseCase listPendingMembershipsUseCase;
    private final ChangeMemberRoleUseCase changeMemberRoleUseCase;
    private final RemoveMemberUseCase removeMemberUseCase;
    private final LeaveCompanyUseCase leaveCompanyUseCase;

    public CompanyMembershipController(
            InviteMemberUseCase inviteMemberUseCase,
            RequestToJoinCompanyUseCase requestToJoinCompanyUseCase,
            ApproveMembershipRequestUseCase approveMembershipRequestUseCase,
            RejectMembershipRequestUseCase rejectMembershipRequestUseCase,
            AcceptCompanyInvitationUseCase acceptCompanyInvitationUseCase,
            DeclineCompanyInvitationUseCase declineCompanyInvitationUseCase,
            ListMyInvitationsUseCase listMyInvitationsUseCase,
            ListCompanyMembersUseCase listCompanyMembersUseCase,
            ListPendingMembershipsUseCase listPendingMembershipsUseCase,
            ChangeMemberRoleUseCase changeMemberRoleUseCase,
            RemoveMemberUseCase removeMemberUseCase,
            LeaveCompanyUseCase leaveCompanyUseCase) {
        this.inviteMemberUseCase = inviteMemberUseCase;
        this.requestToJoinCompanyUseCase = requestToJoinCompanyUseCase;
        this.approveMembershipRequestUseCase = approveMembershipRequestUseCase;
        this.rejectMembershipRequestUseCase = rejectMembershipRequestUseCase;
        this.acceptCompanyInvitationUseCase = acceptCompanyInvitationUseCase;
        this.declineCompanyInvitationUseCase = declineCompanyInvitationUseCase;
        this.listMyInvitationsUseCase = listMyInvitationsUseCase;
        this.listCompanyMembersUseCase = listCompanyMembersUseCase;
        this.listPendingMembershipsUseCase = listPendingMembershipsUseCase;
        this.changeMemberRoleUseCase = changeMemberRoleUseCase;
        this.removeMemberUseCase = removeMemberUseCase;
        this.leaveCompanyUseCase = leaveCompanyUseCase;
    }

    @AuthSecure
    @PostMapping("/{companyId}/members/invite")
    @Operation(summary = "Invite member", description = "Invites a user by email to join the company (role must be below the inviter's role)")
    public ResponseEntity<ApiResponseDTO<CompanyMemberOutputDTO>> inviteMember(
            @PathVariable UUID companyId,
            @RequestBody InviteMemberInputDTO input) throws ApplicationException {
        return created(inviteMemberUseCase.execute(companyId, input));
    }

    @AuthSecure
    @PostMapping("/{companyId}/join")
    @Operation(summary = "Request to join", description = "Requests to join a company as MEMBER (pending company approval)")
    public ResponseEntity<ApiResponseDTO<CompanyMemberOutputDTO>> requestToJoin(
            @PathVariable UUID companyId) throws ApplicationException {
        return created(requestToJoinCompanyUseCase.execute(companyId));
    }

    @AuthSecure
    @GetMapping("/{companyId}/members")
    @Operation(summary = "List members", description = "Lists all approved members of the company")
    public ResponseEntity<ApiResponseDTO<List<CompanyMemberOutputDTO>>> listMembers(
            @PathVariable UUID companyId) throws ApplicationException {
        return ok(listCompanyMembersUseCase.execute(companyId));
    }

    @AuthSecure
    @GetMapping("/{companyId}/members/pending")
    @Operation(summary = "List pending memberships", description = "Lists memberships awaiting approval (approvers only)")
    public ResponseEntity<ApiResponseDTO<List<CompanyMemberOutputDTO>>> listPending(
            @PathVariable UUID companyId) throws ApplicationException {
        return ok(listPendingMembershipsUseCase.execute(companyId));
    }

    @AuthSecure
    @PostMapping("/{companyId}/members/{membershipId}/approve")
    @Operation(summary = "Approve membership", description = "Approves a pending join request (role hierarchy applies)")
    public ResponseEntity<ApiResponseDTO<CompanyMemberOutputDTO>> approveMembership(
            @PathVariable UUID companyId,
            @PathVariable UUID membershipId) throws ApplicationException {
        return ok(approveMembershipRequestUseCase.execute(companyId, membershipId));
    }

    @AuthSecure
    @PostMapping("/{companyId}/members/{membershipId}/reject")
    @Operation(summary = "Reject membership", description = "Rejects a pending join request (role hierarchy applies)")
    public ResponseEntity<ApiResponseDTO<Void>> rejectMembership(
            @PathVariable UUID companyId,
            @PathVariable UUID membershipId) throws ApplicationException {
        rejectMembershipRequestUseCase.execute(companyId, membershipId);
        return noContent();
    }

    @AuthSecure
    @PatchMapping("/{companyId}/members/{membershipId}/role")
    @Operation(summary = "Change member role", description = "Changes a member's role (actor must outrank both current and new role)")
    public ResponseEntity<ApiResponseDTO<CompanyMemberOutputDTO>> changeMemberRole(
            @PathVariable UUID companyId,
            @PathVariable UUID membershipId,
            @RequestBody ChangeMemberRoleInputDTO input) throws ApplicationException {
        return ok(changeMemberRoleUseCase.execute(companyId, membershipId, input));
    }

    @AuthSecure
    @DeleteMapping("/{companyId}/members/{membershipId}")
    @Operation(summary = "Remove member", description = "Removes a member from the company (actor must outrank the member)")
    public ResponseEntity<ApiResponseDTO<Void>> removeMember(
            @PathVariable UUID companyId,
            @PathVariable UUID membershipId) throws ApplicationException {
        removeMemberUseCase.execute(companyId, membershipId);
        return noContent();
    }

    @AuthSecure
    @GetMapping("/invitations")
    @Operation(summary = "My invitations", description = "Lists pending company invitations for the authenticated user")
    public ResponseEntity<ApiResponseDTO<List<CompanyInvitationOutputDTO>>> myInvitations()
            throws ApplicationException {
        return ok(listMyInvitationsUseCase.execute());
    }

    @AuthSecure
    @PostMapping("/{companyId}/invitation/accept")
    @Operation(summary = "Accept invitation", description = "Accepts a pending company invitation")
    public ResponseEntity<ApiResponseDTO<CompanyMemberOutputDTO>> acceptInvitation(
            @PathVariable UUID companyId) throws ApplicationException {
        return ok(acceptCompanyInvitationUseCase.execute(companyId));
    }

    @AuthSecure
    @PostMapping("/{companyId}/invitation/decline")
    @Operation(summary = "Decline invitation", description = "Declines a pending company invitation")
    public ResponseEntity<ApiResponseDTO<Void>> declineInvitation(
            @PathVariable UUID companyId) throws ApplicationException {
        declineCompanyInvitationUseCase.execute(companyId);
        return noContent();
    }

    @AuthSecure
    @PostMapping("/{companyId}/leave")
    @Operation(summary = "Leave company", description = "Leaves the company (OWNER cannot leave)")
    public ResponseEntity<ApiResponseDTO<Void>> leaveCompany(
            @PathVariable UUID companyId) throws ApplicationException {
        leaveCompanyUseCase.execute(companyId);
        return noContent();
    }
}
