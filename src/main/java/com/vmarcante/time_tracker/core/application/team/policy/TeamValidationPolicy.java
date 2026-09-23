package com.vmarcante.time_tracker.core.application.team.policy;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

@Component
public class TeamValidationPolicy {

    public void validateName(String name) throws ApplicationException {
        if (name == null || name.isBlank()) {
            throw new ApplicationException("team.name.required", null);
        }

        String trimmed = name.trim();
        if (trimmed.length() < 2) {
            throw new ApplicationException("team.name.min.length", null);
        }
        if (trimmed.length() > 200) {
            throw new ApplicationException("team.name.max.length", null);
        }
    }
}
