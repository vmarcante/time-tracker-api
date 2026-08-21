package com.vmarcante.time_tracker.core.application.user.auth.dto.input;

import com.vmarcante.time_tracker.core.shared.vo.Email;
import com.vmarcante.time_tracker.core.shared.vo.Phone;

public record CreateUserInputDTO(
        String username,
        String name,
        Integer age,
        Email email,
        Phone phone,
        String password,
        String locale) {

    public CreateUserInputDTO(String username, String name, Integer age, Email email, Phone phone, String password) {
        this(username, name, age, email, phone, password, "pt");
    }
}