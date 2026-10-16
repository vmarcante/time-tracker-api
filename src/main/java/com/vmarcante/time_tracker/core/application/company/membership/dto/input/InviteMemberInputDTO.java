package com.vmarcante.time_tracker.core.application.company.membership.dto.input;

import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.shared.vo.Email;

public record InviteMemberInputDTO(
        Email email,
        CompanyRole role) {
}
