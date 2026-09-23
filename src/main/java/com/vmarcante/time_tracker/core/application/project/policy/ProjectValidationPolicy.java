package com.vmarcante.time_tracker.core.application.project.policy;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;

@Component
public class ProjectValidationPolicy {

    public void validateName(String name) throws ApplicationException {
        if (name == null || name.isBlank()) {
            throw new ApplicationException("project.name.required", null);
        }

        String trimmed = name.trim();
        if (trimmed.length() < 2) {
            throw new ApplicationException("project.name.min.length", null);
        }
        if (trimmed.length() > 200) {
            throw new ApplicationException("project.name.max.length", null);
        }
    }

    public void validateClientName(String clientName) throws ApplicationException {
        if (clientName == null || clientName.isBlank()) {
            throw new ApplicationException("project.client.name.required", null);
        }

        String trimmed = clientName.trim();
        if (trimmed.length() < 2) {
            throw new ApplicationException("project.client.name.min.length", null);
        }
        if (trimmed.length() > 200) {
            throw new ApplicationException("project.client.name.max.length", null);
        }
    }

    public void validateDates(LocalDate startDate, LocalDate endDate) throws ApplicationException {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new ApplicationException("project.dates.invalid", null);
        }
    }

    public void validateStatusTransition(ProjectStatus current, ProjectStatus target)
            throws ApplicationException {
        if (current == target) {
            throw new ApplicationException("project.status.already", null);
        }

        boolean allowed = switch (target) {
            case COMPLETED -> current == ProjectStatus.ACTIVE;
            case ARCHIVED -> current == ProjectStatus.ACTIVE || current == ProjectStatus.COMPLETED;
            case ACTIVE -> current == ProjectStatus.ARCHIVED || current == ProjectStatus.COMPLETED;
        };

        if (!allowed) {
            throw new ApplicationException("project.status.invalid.transition", null);
        }
    }
}
