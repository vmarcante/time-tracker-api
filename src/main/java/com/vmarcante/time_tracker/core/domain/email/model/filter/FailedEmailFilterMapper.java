package com.vmarcante.time_tracker.core.domain.email.model.filter;

public final class FailedEmailFilterMapper {

    private FailedEmailFilterMapper() {
    }

    public static FailedEmailFilter retryableUpTo(int maxRetryCount) {
        FailedEmailFilter filter = new FailedEmailFilter();
        filter.setMaxRetryCount(maxRetryCount);
        return filter;
    }
}
