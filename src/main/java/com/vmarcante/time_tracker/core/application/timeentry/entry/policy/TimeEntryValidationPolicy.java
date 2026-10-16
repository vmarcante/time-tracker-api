package com.vmarcante.time_tracker.core.application.timeentry.entry.policy;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

@Component
public class TimeEntryValidationPolicy {

    public void validateName(String name) throws ApplicationException {
        if (name == null || name.isBlank() || name.trim().length() > 200) {
            throw new ApplicationException("timeentry.name.invalid", null);
        }
    }
}
