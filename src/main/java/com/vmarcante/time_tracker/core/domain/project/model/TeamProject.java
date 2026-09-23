package com.vmarcante.time_tracker.core.domain.project.model;

import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.model.BaseActiveDomainModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeamProject extends BaseActiveDomainModel<UUID, Integer> {

    private UUID projectId;
    private UUID teamId;
    private UUID createdBy;
    private UUID updatedBy;
}
