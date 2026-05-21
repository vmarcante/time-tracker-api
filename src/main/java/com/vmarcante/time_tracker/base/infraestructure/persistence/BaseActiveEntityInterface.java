package com.vmarcante.time_tracker.base.infraestructure.persistence;

public interface BaseActiveEntityInterface<ID, SID> extends BaseEntityInterface<ID, SID> {

    public Boolean getActive();

    public void setActive(Boolean active);
}
