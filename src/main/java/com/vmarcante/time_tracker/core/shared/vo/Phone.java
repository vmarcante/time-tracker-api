package com.vmarcante.time_tracker.core.shared.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;
import com.vmarcante.time_tracker.core.shared.vo.exception.VoException;

public class Phone {

    private final String number;

    @JsonCreator
    public Phone(String number) {
        if (StringValidationUtils.containsContent(number)) {
            String cleanNumber = PhoneValidator.clean(number);

            if (!PhoneValidator.isValid(number)) {
                throw new VoException("field.phone.invalid");
            }

            number = cleanNumber;
        }

        this.number = number;
    }

    @JsonValue
    public String number() {
        return number;
    }

    public String formatted() {
        if (!StringValidationUtils.containsContent(number)) {
            return "";
        }

        switch (number.length()) {
            case 8:
                // XXXX-XXXX
                return String.format("%s-%s",
                        number.substring(0, 4),
                        number.substring(4));
            case 9:
                // XXXXX-XXXX
                return String.format("%s-%s",
                        number.substring(0, 5),
                        number.substring(5));
            case 10:
                // (XX) XXXX-XXXX
                return String.format("(%s) %s-%s",
                        number.substring(0, 2),
                        number.substring(2, 6),
                        number.substring(6));
            case 11:
                // (XX) 9XXXX-XXXX
                return String.format("(%s) %s-%s",
                        number.substring(0, 2),
                        number.substring(2, 7),
                        number.substring(7));
            default:
                return number;
        }
    }

    public String raw() {
        if (number == null || number.isEmpty()) {
            return "";
        }

        // Removes all non-digit characters
        return number.replaceAll("\\D", "");
    }
}