package com.vmarcante.time_tracker.core.shared.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.vmarcante.time_tracker.core.shared.vo.exception.VoException;

public class Email {

    private final String address;

    @JsonCreator
    public Email(String address) {
        if (!EmailValidator.isValid(address)) {
            throw new VoException("field.email.invalid");
        }
        this.address = address;
    }

    @JsonValue
    public String address() {
        return address;
    }
}

