package com.vmarcante.time_tracker.core.domain.team.model;

import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.model.BaseActiveDomainModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Team extends BaseActiveDomainModel<UUID, Integer> {

    private UUID companyId;
    private String name;
    private String description;
    private UUID createdBy;
    private UUID updatedBy;
}
