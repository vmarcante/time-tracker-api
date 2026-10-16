package com.vmarcante.time_tracker.base.infrastructure.persistence;

public interface BaseAuditedEntity<ID, SEQ_ID> extends BaseActiveEntity<ID, SEQ_ID> {

    public ID getCreatedBy();

    public void setCreatedBy(ID createdBy);

    public ID getUpdatedBy();

    public void setUpdatedBy(ID updatedBy);
}
