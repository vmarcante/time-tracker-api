package com.vmarcante.time_tracker.core.shared.vo;

public class CnpjValidator {

    private static final String CNPJ_FORMATTED_PATTERN = "^\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}$";

    private CnpjValidator() {
    }

    public static boolean isValid(String cnpj) {
        if (cnpj == null || cnpj.isBlank()) {
            return false;
        }

        String trimmed = cnpj.trim();
        String digits = trimmed.replaceAll("\\D", "");

        if (trimmed.matches("^\\d+$")) {
            if (digits.length() > 14) {
                return false;
            }
            digits = leftPad(digits);
        } else if (!trimmed.matches(CNPJ_FORMATTED_PATTERN)) {
            return false;
        }

        return hasValidCheckDigits(digits);
    }

    static String leftPad(String digits) {
        return String.format("%14s", digits).replace(' ', '0');
    }

    private static boolean hasValidCheckDigits(String cnpj) {
        if (cnpj == null) {
            return false;
        }

        String digits = cnpj.replaceAll("[^\\d]", "");

        if (digits.length() != 14) {
            return false;
        }

        if (digits.chars().distinct().count() == 1) {
            return false;
        }

        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += Character.getNumericValue(digits.charAt(i)) * weights1[i];
        }

        int remainder = sum % 11;
        int digit1 = remainder < 2 ? 0 : 11 - remainder;

        if (Character.getNumericValue(digits.charAt(12)) != digit1) {
            return false;
        }

        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += Character.getNumericValue(digits.charAt(i)) * weights2[i];
        }
        remainder = sum % 11;
        int digit2 = remainder < 2 ? 0 : 11 - remainder;

        boolean isValid = Character.getNumericValue(digits.charAt(13)) == digit2;
        return isValid;
    }
}
