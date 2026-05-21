package com.vmarcante.time_tracker.base.infraestructure.persistence;

public interface BaseAuthEntityInterface<ID, SEQ_ID> extends BaseActiveEntityInterface<ID, SEQ_ID> {

    public ID getCreatedBy();

    public void setCreatedBy(ID createdBy);

    public ID getUpdatedBy();

    public void setUpdatedBy(ID updatedBy);
}
