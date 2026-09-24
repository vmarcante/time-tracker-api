package com.vmarcante.time_tracker.core.application.user.auth.dto.input;

import com.vmarcante.time_tracker.core.shared.vo.Cnpj;

public record OnboardingInputDTO(
        Cnpj document,
        Boolean independent,
        String requestReason) {
}
