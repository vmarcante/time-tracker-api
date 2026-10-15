package com.vmarcante.time_tracker.core.domain.company.event;

import java.util.UUID;

import lombok.Data;

@Data
public class MembershipApprovedEvent {

    private final UUID membershipId;
    private final UUID memberUserId;
    private final UUID companyId;

    public MembershipApprovedEvent(UUID membershipId, UUID memberUserId, UUID companyId) {
        this.membershipId = membershipId;
        this.memberUserId = memberUserId;
        this.companyId = companyId;
    }
}
