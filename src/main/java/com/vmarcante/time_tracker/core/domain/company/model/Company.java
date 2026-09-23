package com.vmarcante.time_tracker.core.domain.company.model;

import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.model.BaseActiveDomainModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Company extends BaseActiveDomainModel<UUID, Integer> {

    private String legalName;
    private String tradeName;
    private String document;
    private String description;
    private String timezone;
    private UUID createdBy;
    private UUID updatedBy;
}
