package com.vmarcante.time_tracker.core.domain.team.model;

import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.model.BaseActiveDomainModel;
import com.vmarcante.time_tracker.core.domain.team.enums.TeamRole;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeamMembership extends BaseActiveDomainModel<UUID, Integer> {

    private UUID userId;
    private UUID teamId;
    private TeamRole role;
    private Boolean approved;
    private UUID createdBy;
    private UUID updatedBy;
}
