package com.vmarcante.time_tracker.core.application.company.dto.input;

import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;

public record ChangeMemberRoleInputDTO(
        CompanyRole role) {
}
