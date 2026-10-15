package com.vmarcante.time_tracker.core.domain.company.event;

import java.util.UUID;

import lombok.Data;

@Data
public class MembershipRejectedEvent {

    private final UUID memberUserId;
    private final UUID companyId;
    private final String rejectionReason;

    public MembershipRejectedEvent(UUID memberUserId, UUID companyId, String rejectionReason) {
        this.memberUserId = memberUserId;
        this.companyId = companyId;
        this.rejectionReason = rejectionReason;
    }
}
