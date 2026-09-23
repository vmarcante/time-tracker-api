package com.vmarcante.time_tracker.core.domain.timeentry.model;

import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.model.BaseActiveDomainModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TimeEntry extends BaseActiveDomainModel<UUID, Integer> {

    private UUID userId;
    private UUID companyId;
    private UUID projectId;
    private String name;
    private String description;
    private UUID createdBy;
    private UUID updatedBy;
}
