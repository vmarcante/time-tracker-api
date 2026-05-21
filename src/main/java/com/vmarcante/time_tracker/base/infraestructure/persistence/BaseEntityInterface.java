package com.vmarcante.time_tracker.base.infraestructure.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

// ID -> ID
// SID -> Sequential ID
public interface BaseEntityInterface<ID, SID> {

    public ID getId();

    public void setId(ID id);

    public LocalDateTime getCreatedAt();

    public void setCreatedAt(LocalDateTime createdAt);

    public LocalDateTime getUpdatedAt();

    public void setUpdatedAt(LocalDateTime updatedAt);

    public SID getSeqId();

    public void setSeqId(SID seqId);

    @PrePersist
    public void onCreate();

    @PreUpdate
    public void onUpdate();

}
