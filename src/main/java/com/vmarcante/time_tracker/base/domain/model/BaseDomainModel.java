package com.vmarcante.time_tracker.base.domain.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class BaseDomainModel<ID, SID> {
    private ID id;
    private SID seqId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
