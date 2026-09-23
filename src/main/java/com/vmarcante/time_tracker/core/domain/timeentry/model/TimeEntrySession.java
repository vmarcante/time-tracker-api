package com.vmarcante.time_tracker.core.domain.timeentry.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.model.BaseActiveDomainModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TimeEntrySession extends BaseActiveDomainModel<UUID, Integer> {

    private UUID entryId;
    private UUID userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private UUID createdBy;
    private UUID updatedBy;
}
