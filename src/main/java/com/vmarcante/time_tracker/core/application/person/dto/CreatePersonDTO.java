package com.vmarcante.time_tracker.core.application.person.dto;

import com.vmarcante.time_tracker.core.shared.vo.Email;
import com.vmarcante.time_tracker.core.shared.vo.Phone;

public record CreatePersonDTO(
        String name,
        Integer age,
        Email email,
        Phone phone,
        String locale) {

    public CreatePersonDTO(String name, Integer age, Email email, Phone phone) {
        this(name, age, email, phone, "pt");
    }
}
