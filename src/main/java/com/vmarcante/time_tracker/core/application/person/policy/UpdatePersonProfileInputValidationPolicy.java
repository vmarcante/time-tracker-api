package com.vmarcante.time_tracker.core.application.person.policy;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.person.dto.UpdatePersonProfileInputDTO;
import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;

@Component
public class UpdatePersonProfileInputValidationPolicy {

    public void validate(UpdatePersonProfileInputDTO input) throws ApplicationException {
        validateName(input.name());
        validateAge(input.age());
    }

    private void validateName(String name) throws ApplicationException {
        if (!StringValidationUtils.containsContent(name)) {
            throw new ApplicationException("user.name.required", null);
        }

        String trimmedName = name.trim();

        if (trimmedName.length() < 3) {
            throw new ApplicationException("user.name.min.length", null);
        }

        if (trimmedName.length() > 100) {
            throw new ApplicationException("user.name.max.length", null);
        }

        if (!trimmedName.matches("^[a-zA-ZÀ-ÿ\\s'-]+$")) {
            throw new ApplicationException("user.name.invalid.characters", null);
        }

        if (trimmedName.matches(".*\\d.*")) {
            throw new ApplicationException("user.name.no.numbers", null);
        }

        if (StringValidationUtils.containsEmoji(trimmedName)) {
            throw new ApplicationException("user.name.no.emoji", null);
        }

        if (trimmedName.matches(".*\\s{2,}.*")) {
            throw new ApplicationException("user.name.multiple.spaces", null);
        }

        String[] nameParts = trimmedName.split("\\s+");
        if (nameParts.length < 2) {
            throw new ApplicationException("user.name.missing.surname", null);
        }

        for (String part : nameParts) {
            if (part.length() < 2) {
                throw new ApplicationException("user.name.part.too.short", null);
            }
        }
    }

    /**
     * Age is optional in profile updates. If provided, must be between 18 and 120.
     */
    private void validateAge(Integer age) throws ApplicationException {
        if (age == null) {
            return;
        }

        if (age < 18) {
            throw new ApplicationException("user.age.min", null);
        }

        if (age > 120) {
            throw new ApplicationException("user.age.max", null);
        }
    }
}
