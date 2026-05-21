package com.vmarcante.time_tracker.core.application.user.auth.policy;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.CreateUserAuthDTO;
import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;

@Component
public class CreateUserInputValidationPolicy {

    public void validate(CreateUserAuthDTO input) throws ApplicationException {
        validateUsername(input.username());
        validatePassword(input.password());
    }

    public void validatePassword(String password) throws ApplicationException {
        if (!StringValidationUtils.containsContent(password)) {
            throw new ApplicationException("user.password.required", null);
        }

        if (password.length() < 8) {
            throw new ApplicationException("user.password.min.length", null);
        }

        if (password.length() > 128) {
            throw new ApplicationException("user.password.max.length", null);
        }

        if (StringValidationUtils.containsEmoji(password)) {
            throw new ApplicationException("user.password.no.emoji", null);
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new ApplicationException("user.password.missing.uppercase", null);
        }

        if (!password.matches(".*[a-z].*")) {
            throw new ApplicationException("user.password.missing.lowercase", null);
        }

        if (!password.matches(".*[0-9].*")) {
            throw new ApplicationException("user.password.missing.number", null);
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new ApplicationException("user.password.missing.special", null);
        }

        if (password.contains(" ")) {
            throw new ApplicationException("user.password.no.spaces", null);
        }
    }

    private void validateUsername(String username) throws ApplicationException {
        if (!StringValidationUtils.containsContent(username)) {
            throw new ApplicationException("user.username.required", null);
        }

        if (username.length() < 3) {
            throw new ApplicationException("user.username.min.length", null);
        }

        if (username.length() > 30) {
            throw new ApplicationException("user.username.max.length", null);
        }

        if (username.contains(" ")) {
            throw new ApplicationException("user.username.no.spaces", null);
        }

        if (!username.matches("^[a-zA-Z0-9._]+$")) {
            throw new ApplicationException("user.username.invalid.characters", null);
        }

        if (username.startsWith(".") || username.startsWith("_")) {
            throw new ApplicationException("user.username.invalid.start", null);
        }

        if (username.endsWith(".") || username.endsWith("_")) {
            throw new ApplicationException("user.username.invalid.end", null);
        }

        if (username.contains("..") || username.contains("__") || username.contains("._") || username.contains("_.")) {
            throw new ApplicationException("user.username.invalid.consecutive", null);
        }
    }
}
