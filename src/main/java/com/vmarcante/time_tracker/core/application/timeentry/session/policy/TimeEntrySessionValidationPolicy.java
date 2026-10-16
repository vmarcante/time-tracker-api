package com.vmarcante.time_tracker.core.application.timeentry.session.policy;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

@Component
public class TimeEntrySessionValidationPolicy {

    public void validateSessionTimes(LocalDateTime startTime, LocalDateTime endTime) throws ApplicationException {
        if (startTime == null) {
            throw new ApplicationException("timeentry.session.start.required", null);
        }
        if (endTime != null && !endTime.isAfter(startTime)) {
            throw new ApplicationException("timeentry.session.end.before.start", null);
        }
    }
}
