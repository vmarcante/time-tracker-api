package com.vmarcante.time_tracker.core.shared.vo;

import java.util.regex.Pattern;

public class EmailValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    private EmailValidator() {
    }

    public static boolean isValid(String address) {
        // Max length or null check
        if (address == null || address.length() >= 255) {
            return false;
        }

        // Pattern Validation
        if (!EMAIL_PATTERN.matcher(address).matches()) {
            return false;
        }

        // Local part validation (before @)
        String[] parts = address.split("@");
        if (parts[0].length() >= 65) {
            return false;
        }

        return true;
    }

}
