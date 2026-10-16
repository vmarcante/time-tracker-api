package com.vmarcante.time_tracker.core.application.timeentry.tag.policy;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

@Component
public class TagValidationPolicy {

    private static final Pattern COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    public void validateTag(String name, String color) throws ApplicationException {
        if (name == null || name.isBlank() || name.trim().length() > 50) {
            throw new ApplicationException("tag.name.invalid", null);
        }
        if (color == null || !COLOR_PATTERN.matcher(color).matches()) {
            throw new ApplicationException("tag.color.invalid", null);
        }
    }
}
