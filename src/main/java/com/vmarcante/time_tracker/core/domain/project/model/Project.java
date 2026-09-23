package com.vmarcante.time_tracker.core.domain.project.model;

import java.time.LocalDate;
import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.model.BaseActiveDomainModel;
import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Project extends BaseActiveDomainModel<UUID, Integer> {

    private UUID companyId;
    private String name;
    private String description;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID createdBy;
    private UUID updatedBy;
}
