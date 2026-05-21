package com.vmarcante.time_tracker.core.domain.email.model;

import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.model.BaseDomainModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FailedEmail extends BaseDomainModel<UUID, Integer> {
    private String recipient;
    private String subject;
    private String templateName;
    private String templateData;
    private String locale;
    private String errorMessage;
    private String stackTrace;
    private Integer retryCount;
}
