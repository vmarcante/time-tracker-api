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
}
