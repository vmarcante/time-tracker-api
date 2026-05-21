package com.vmarcante.time_tracker.core.domain.email.model.filter;

import lombok.Data;

@Data
public class FailedEmailFilter {

    private String recipient;
    private String subject;
    private String templateName;
    private String locale;
    private Integer retryCount;
    private Integer maxRetryCount;
    private String recipientContains;
    private String subjectContains;
}
