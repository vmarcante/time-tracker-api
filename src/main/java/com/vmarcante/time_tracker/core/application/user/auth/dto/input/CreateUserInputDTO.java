package com.vmarcante.time_tracker.core.application.user.auth.dto.input;

import com.vmarcante.time_tracker.core.shared.vo.Email;
import com.vmarcante.time_tracker.core.shared.vo.Phone;

public record CreateUserInputDTO(
                String username,
                String name,
                Email email,
                Phone phone,
                String password,
                String locale) {
    public CreateUserInputDTO(String username, String name, Email email, Phone phone, String password) {
        this(username, name, email, phone, password, "pt");
    }
}