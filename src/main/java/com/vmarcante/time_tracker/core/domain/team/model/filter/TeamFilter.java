package com.vmarcante.time_tracker.core.domain.team.model.filter;

import java.util.Collection;
import java.util.UUID;

import lombok.Data;

@Data
public class TeamFilter {
    private UUID id;
    private Collection<UUID> ids;
    private UUID companyId;
    private String name;
    private Boolean active;
    private UUID memberUserId;
    private UUID linkedProjectId;
}
