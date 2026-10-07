package com.vmarcante.time_tracker.core.domain.person.enums;

import java.util.Arrays;
import java.util.Optional;

public enum SupportedLocale {

    PT_BR("pt-BR"),
    EN_US("en-US");

    private final String code;

    SupportedLocale(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Optional<SupportedLocale> fromCode(String code) {
        return Arrays.stream(values())
                .filter(locale -> locale.code.equalsIgnoreCase(code))
                .findFirst();
    }

    public static boolean isSupported(String code) {
        return fromCode(code).isPresent();
    }
}
