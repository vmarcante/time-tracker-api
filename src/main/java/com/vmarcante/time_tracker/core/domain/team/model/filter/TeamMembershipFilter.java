package com.vmarcante.time_tracker.core.domain.team.model.filter;

import java.util.Collection;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.team.enums.TeamRole;

import lombok.Data;

@Data
public class TeamMembershipFilter {
    private UUID id;
    private UUID userId;
    private UUID teamId;
    private Collection<UUID> teamIds;
    private UUID companyId;
    private TeamRole role;
    private Boolean approved;
    private Boolean active;
}
