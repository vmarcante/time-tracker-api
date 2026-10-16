package com.vmarcante.time_tracker.core.interfaces.company.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.company.dto.input.MembershipFilterInputDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.company.dto.input.ChangeMemberRoleInputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.input.InviteMemberInputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.input.PreRegisterAndInviteInputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.input.RejectMembershipInputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyInvitationOutputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.PersonInviteLookupOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.AcceptCompanyInvitationUseCase;
import com.vmarcante.time_tracker.core.application.company.in.ApproveMembershipRequestUseCase;
import com.vmarcante.time_tracker.core.application.company.in.ChangeMemberRoleUseCase;
import com.vmarcante.time_tracker.core.application.company.in.DeclineCompanyInvitationUseCase;
import com.vmarcante.time_tracker.core.application.company.in.InviteMemberUseCase;
import com.vmarcante.time_tracker.core.application.company.in.LeaveCompanyUseCase;
import com.vmarcante.time_tracker.core.application.company.in.ListCompanyMembersUseCase;
import com.vmarcante.time_tracker.core.application.company.in.ListMyInvitationsUseCase;
import com.vmarcante.time_tracker.core.application.company.in.ListPendingMembershipsUseCase;
import com.vmarcante.time_tracker.core.application.company.in.ListResolvedMembershipsUseCase;
import com.vmarcante.time_tracker.core.application.company.in.LookupPersonForInviteUseCase;
import com.vmarcante.time_tracker.core.application.company.in.PreRegisterAndInviteUseCase;
import com.vmarcante.time_tracker.core.application.company.in.RejectMembershipRequestUseCase;
import com.vmarcante.time_tracker.core.application.company.in.RemoveMemberUseCase;
import com.vmarcante.time_tracker.core.application.company.dto.input.RequestToJoinInputDTO;
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
    private final LookupPersonForInviteUseCase lookupPersonForInviteUseCase;
    private final PreRegisterAndInviteUseCase preRegisterAndInviteUseCase;
    private final RequestToJoinCompanyUseCase requestToJoinCompanyUseCase;
    private final ApproveMembershipRequestUseCase approveMembershipRequestUseCase;
    private final RejectMembershipRequestUseCase rejectMembershipRequestUseCase;
    private final AcceptCompanyInvitationUseCase acceptCompanyInvitationUseCase;
    private final DeclineCompanyInvitationUseCase declineCompanyInvitationUseCase;
    private final ListMyInvitationsUseCase listMyInvitationsUseCase;
    private final ListCompanyMembersUseCase listCompanyMembersUseCase;
    private final ListPendingMembershipsUseCase listPendingMembershipsUseCase;
    private final ListResolvedMembershipsUseCase listResolvedMembershipsUseCase;
    private final ChangeMemberRoleUseCase changeMemberRoleUseCase;
    private final RemoveMemberUseCase removeMemberUseCase;
    private final LeaveCompanyUseCase leaveCompanyUseCase;

    public CompanyMembershipController(
            InviteMemberUseCase inviteMemberUseCase,
            LookupPersonForInviteUseCase lookupPersonForInviteUseCase,
            PreRegisterAndInviteUseCase preRegisterAndInviteUseCase,
            RequestToJoinCompanyUseCase requestToJoinCompanyUseCase,
            ApproveMembershipRequestUseCase approveMembershipRequestUseCase,
            RejectMembershipRequestUseCase rejectMembershipRequestUseCase,
            AcceptCompanyInvitationUseCase acceptCompanyInvitationUseCase,
            DeclineCompanyInvitationUseCase declineCompanyInvitationUseCase,
            ListMyInvitationsUseCase listMyInvitationsUseCase,
            ListCompanyMembersUseCase listCompanyMembersUseCase,
            ListPendingMembershipsUseCase listPendingMembershipsUseCase,
            ListResolvedMembershipsUseCase listResolvedMembershipsUseCase,
            ChangeMemberRoleUseCase changeMemberRoleUseCase,
            RemoveMemberUseCase removeMemberUseCase,
            LeaveCompanyUseCase leaveCompanyUseCase) {
        this.inviteMemberUseCase = inviteMemberUseCase;
        this.lookupPersonForInviteUseCase = lookupPersonForInviteUseCase;
        this.preRegisterAndInviteUseCase = preRegisterAndInviteUseCase;
        this.requestToJoinCompanyUseCase = requestToJoinCompanyUseCase;
        this.approveMembershipRequestUseCase = approveMembershipRequestUseCase;
        this.rejectMembershipRequestUseCase = rejectMembershipRequestUseCase;
        this.acceptCompanyInvitationUseCase = acceptCompanyInvitationUseCase;
        this.declineCompanyInvitationUseCase = declineCompanyInvitationUseCase;
        this.listMyInvitationsUseCase = listMyInvitationsUseCase;
        this.listCompanyMembersUseCase = listCompanyMembersUseCase;
        this.listPendingMembershipsUseCase = listPendingMembershipsUseCase;
        this.listResolvedMembershipsUseCase = listResolvedMembershipsUseCase;
        this.changeMemberRoleUseCase = changeMemberRoleUseCase;
        this.removeMemberUseCase = removeMemberUseCase;
        this.leaveCompanyUseCase = leaveCompanyUseCase;
    }

    @AuthSecure
    @GetMapping("/{companyId}/members/lookup")
    @Operation(summary = "Lookup person for invite", description = "Checks if a person exists by email and whether they are available to invite")
    public ResponseEntity<ApiResponseDTO<PersonInviteLookupOutputDTO>> lookupPersonForInvite(
            @PathVariable UUID companyId,
            @RequestParam String email) throws ApplicationException {
        return ok(lookupPersonForInviteUseCase.execute(companyId, email));
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
    @PostMapping("/{companyId}/members/pre-register-and-invite")
    @Operation(summary = "Pre-register and invite", description = "Pre-registers a person without an account and sends them an invitation email to complete registration")
    public ResponseEntity<ApiResponseDTO<CompanyMemberOutputDTO>> preRegisterAndInvite(
            @PathVariable UUID companyId,
            @RequestBody PreRegisterAndInviteInputDTO input) throws ApplicationException {
        return created(preRegisterAndInviteUseCase.execute(companyId, input));
    }

    @AuthSecure
    @PostMapping("/{companyId}/join")
    @Operation(summary = "Request to join", description = "Requests to join a company as MEMBER (pending company approval)")
    public ResponseEntity<ApiResponseDTO<CompanyMemberOutputDTO>> requestToJoin(
            @PathVariable UUID companyId,
            @RequestBody(required = false) RequestToJoinInputDTO input) throws ApplicationException {
        String requestReason = input != null ? input.requestReason() : null;
        return created(requestToJoinCompanyUseCase.execute(companyId, requestReason));
    }

    @AuthSecure
    @GetMapping("/{companyId}/members")
    @Operation(summary = "List members", description = "Lists all approved members of the company")
    public ResponseEntity<ApiResponseDTO<PageWrapperDTO<CompanyMemberOutputDTO>>> listMembers(
            @PathVariable UUID companyId,
            MembershipFilterInputDTO filter) throws ApplicationException {
        return ok(listCompanyMembersUseCase.execute(companyId, filter));
    }

    @AuthSecure
    @GetMapping("/{companyId}/members/pending")
    @Operation(summary = "List pending memberships", description = "Lists memberships awaiting approval (approvers only)")
    public ResponseEntity<ApiResponseDTO<PageWrapperDTO<CompanyMemberOutputDTO>>> listPending(
            @PathVariable UUID companyId,
            MembershipFilterInputDTO filter) throws ApplicationException {
        return ok(listPendingMembershipsUseCase.execute(companyId, filter));
    }

    @AuthSecure
    @GetMapping("/{companyId}/members/resolved")
    @Operation(summary = "List resolved memberships", description = "Lists all non-pending memberships (approved or rejected) of the company (approvers only)")
    public ResponseEntity<ApiResponseDTO<PageWrapperDTO<CompanyMemberOutputDTO>>> listResolved(
            @PathVariable UUID companyId,
            MembershipFilterInputDTO filter) throws ApplicationException {
        return ok(listResolvedMembershipsUseCase.execute(companyId, filter));
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
            @PathVariable UUID membershipId,
            @RequestBody(required = false) RejectMembershipInputDTO input) throws ApplicationException {
        rejectMembershipRequestUseCase.execute(companyId, membershipId, input);
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

    @AuthSecure(allowPendingOnboarding = true)
    @GetMapping("/invitations")
    @Operation(summary = "My invitations", description = "Lists pending company invitations for the authenticated user")
    public ResponseEntity<ApiResponseDTO<PageWrapperDTO<CompanyInvitationOutputDTO>>> myInvitations(
            MembershipFilterInputDTO filter) throws ApplicationException {
        return ok(listMyInvitationsUseCase.execute(filter));
    }

    @AuthSecure(allowPendingOnboarding = true)
    @PostMapping("/{companyId}/invitation/accept")
    @Operation(summary = "Accept invitation", description = "Accepts a pending company invitation")
    public ResponseEntity<ApiResponseDTO<CompanyMemberOutputDTO>> acceptInvitation(
            @PathVariable UUID companyId) throws ApplicationException {
        return ok(acceptCompanyInvitationUseCase.execute(companyId));
    }

    @AuthSecure(allowPendingOnboarding = true)
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
