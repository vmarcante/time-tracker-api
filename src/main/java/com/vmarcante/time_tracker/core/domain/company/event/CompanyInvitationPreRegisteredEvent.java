package com.vmarcante.time_tracker.core.domain.company.event;

import java.util.UUID;

import lombok.Data;

@Data
public class CompanyInvitationPreRegisteredEvent {

    private final UUID membershipId;
    private final UUID inviteePersonId;
    private final UUID companyId;
    private final UUID inviterUserId;
    private final String inviteeEmail;

    public CompanyInvitationPreRegisteredEvent(
            UUID membershipId,
            UUID inviteePersonId,
            UUID companyId,
            UUID inviterUserId,
            String inviteeEmail) {
        this.membershipId = membershipId;
        this.inviteePersonId = inviteePersonId;
        this.companyId = companyId;
        this.inviterUserId = inviterUserId;
        this.inviteeEmail = inviteeEmail;
    }
}
