package com.vmarcante.time_tracker.base.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BaseActiveDomainModel<ID, SID> extends BaseDomainModel<ID, SID> {
    private Boolean active;
}
