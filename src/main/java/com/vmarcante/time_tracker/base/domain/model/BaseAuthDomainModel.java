package com.vmarcante.time_tracker.base.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BaseAuthDomainModel<ID, SID> extends BaseActiveDomainModel<ID, SID> {
    private ID createdBy;
    private ID updatedBy;
}
