package com.vmarcante.time_tracker.core.application.timeentry.policy;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

@Component
public class TimeEntryValidationPolicy {

    private static final Pattern COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    public void validateName(String name) throws ApplicationException {
        if (name == null || name.isBlank() || name.trim().length() > 200) {
            throw new ApplicationException("timeentry.name.invalid", null);
        }
    }

    public void validateSessionTimes(LocalDateTime startTime, LocalDateTime endTime) throws ApplicationException {
        if (startTime == null) {
            throw new ApplicationException("timeentry.session.start.required", null);
        }
        if (endTime != null && !endTime.isAfter(startTime)) {
            throw new ApplicationException("timeentry.session.end.before.start", null);
        }
    }

    public void validateTag(String name, String color) throws ApplicationException {
        if (name == null || name.isBlank() || name.trim().length() > 50) {
            throw new ApplicationException("tag.name.invalid", null);
        }
        if (color == null || !COLOR_PATTERN.matcher(color).matches()) {
            throw new ApplicationException("tag.color.invalid", null);
        }
    }
}
