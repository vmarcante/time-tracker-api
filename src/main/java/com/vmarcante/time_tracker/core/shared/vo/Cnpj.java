package com.vmarcante.time_tracker.core.shared.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.vmarcante.time_tracker.core.shared.vo.exception.VoException;

public class Cnpj {

    private final String value;

    @JsonCreator
    public Cnpj(String value) {
        if (value == null || value.isBlank()) {
            throw new VoException("field.cnpj.required");
        }

        String trimmed = value.trim();

        if (!CnpjValidator.isValid(trimmed)) {
            throw new VoException("field.cnpj.invalid");
        }

        this.value = trimmed;
    }

    @JsonValue
    public String value() {
        return value;
    }

    public String digits() {
        return value.replaceAll("[^\\d]", "");
    }
}
