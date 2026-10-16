package com.vmarcante.time_tracker.core.application.company.membership.dto.input;

import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;

public record PreRegisterAndInviteInputDTO(
        String name,
        String email,
        String phone,
        Integer age,
        CompanyRole role) {
}
