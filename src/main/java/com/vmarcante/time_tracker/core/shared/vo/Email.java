package com.vmarcante.time_tracker.core.shared.vo;

import com.vmarcante.time_tracker.core.shared.vo.exception.VoException;

public record Email(String address) {

    public Email {
        if (!EmailValidator.isValid(address)) {
            throw new VoException("field.email.invalid");
        }
    }
}
