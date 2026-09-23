package com.vmarcante.time_tracker.core.domain.company.event;

import java.util.UUID;

import lombok.Data;

@Data
public class CompanyInvitationCreatedEvent {

    private final UUID membershipId;
    private final UUID inviteeUserId;
    private final UUID companyId;
    private final UUID inviterUserId;
}
