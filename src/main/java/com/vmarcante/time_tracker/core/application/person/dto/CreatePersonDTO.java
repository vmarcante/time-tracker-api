package com.vmarcante.time_tracker.core.application.person.dto;

import com.vmarcante.time_tracker.core.shared.vo.Email;
import com.vmarcante.time_tracker.core.shared.vo.Phone;

public record CreatePersonDTO(
        String name,
        Email email,
        Phone phone,
        String locale,
        Integer age) {

    public CreatePersonDTO(String name, Email email, Phone phone, String locale) {
        this(name, email, phone, locale, null);
    }

    public CreatePersonDTO(String name, Email email, Phone phone) {
        this(name, email, phone, "pt", null);
    }
}
