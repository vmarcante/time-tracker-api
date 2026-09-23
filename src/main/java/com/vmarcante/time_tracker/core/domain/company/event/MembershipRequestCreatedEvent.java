package com.vmarcante.time_tracker.core.domain.company.event;

import java.util.UUID;

import lombok.Data;

@Data
public class MembershipRequestCreatedEvent {

    private final UUID membershipId;
    private final UUID requesterUserId;
    private final UUID companyId;
}
