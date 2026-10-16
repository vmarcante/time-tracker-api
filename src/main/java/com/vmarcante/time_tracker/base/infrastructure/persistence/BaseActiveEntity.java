package com.vmarcante.time_tracker.base.infrastructure.persistence;

public interface BaseActiveEntity<ID, SID> extends BaseEntity<ID, SID> {

    public Boolean getActive();

    public void setActive(Boolean active);
}
